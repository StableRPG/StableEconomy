package me.jeremiah.economy.storage.databases;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.jeremiah.economy.config.BasicConfig;
import me.jeremiah.economy.storage.BalanceEntry;
import me.jeremiah.economy.storage.DataUtils;
import me.jeremiah.economy.storage.DatabaseInfo;
import me.jeremiah.economy.storage.PlayerAccount;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.HashMap;
import java.util.logging.Level;

public abstract sealed class AbstractSQLDatabase extends Database permits H2, SQLite, PostgreSQL, MariaDB, MySQL {

  protected final HikariConfig hikariConfig = new HikariConfig();
  private final HikariDataSource dataSource;

  AbstractSQLDatabase(Class<? extends Driver> driver, @NotNull BasicConfig config) {
    super(config);
    if (DriverManager.drivers().noneMatch(driver::isInstance))
      try {
        DriverManager.registerDriver(driver.getDeclaredConstructor().newInstance());
      } catch (Exception exception) {
        throw new RuntimeException("Failed to register SQL driver: " + driver.getName(), exception);
      }

    processConfig(config.getDatabaseInfo());

    hikariConfig.setAutoCommit(false);

    dataSource = new HikariDataSource(hikariConfig);

    setup();
  }

  abstract void processConfig(@NotNull DatabaseInfo databaseInfo);

  @Override
  int lookupEntryCount() {
    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM player_entries;");
      if (resultSet.next())
        return resultSet.getInt(1);
    } catch (SQLException exception) {
      config.getPlugin().getLogger().log(Level.SEVERE, "Failed to lookup entry count from SQL database.", exception);
    }
    return super.lookupEntryCount();
  }

  @Override
  void load() {
    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement()) {
      statement.execute("CREATE TABLE IF NOT EXISTS player_entries(uniqueId BINARY(16) PRIMARY KEY, username VARCHAR(32));");
      statement.execute("CREATE TABLE IF NOT EXISTS balance_entries(uniqueId BINARY(16), currency VARCHAR(16), balance DOUBLE, FOREIGN KEY(uniqueId) REFERENCES player_entries(uniqueId), UNIQUE(uniqueId, currency));");
      connection.commit();

      ResultSet rawBalanceEntries = statement.executeQuery("SELECT * FROM balance_entries;");
      HashMap<byte[], HashMap<String, BalanceEntry>> balanceEntries = new HashMap<>();

      while (rawBalanceEntries.next()) {
        byte[] rawUniqueId = rawBalanceEntries.getBytes("uniqueId");

        if (!balanceEntries.containsKey(rawUniqueId))
          balanceEntries.put(rawUniqueId, new HashMap<>());

        String currency = rawBalanceEntries.getString("currency");
        double balance = rawBalanceEntries.getDouble("balance");

        balanceEntries.get(rawUniqueId).put(currency, new BalanceEntry(currency, balance));
      }

      ResultSet rawPlayerEntries = statement.executeQuery("SELECT * FROM player_entries;");

      while (rawPlayerEntries.next()) {
        byte[] rawUniqueId = rawPlayerEntries.getBytes("uniqueId");

        add(new PlayerAccount(
          DataUtils.uuidFromBytes(rawUniqueId),
          rawPlayerEntries.getString("username"),
          balanceEntries.getOrDefault(rawUniqueId, new HashMap<>())
        ));
      }

    } catch (SQLException exception) {
      config.getPlugin().getLogger().log(Level.SEVERE, "Failed to load data from SQL database.", exception);
    }
  }

  String getSavePlayerStatement() {
    return "INSERT INTO player_entries(uniqueId, username) VALUES(?, ?) ON DUPLICATE KEY UPDATE username = VALUES(username);";
  }

  String getSaveBalanceStatement() {
    return "INSERT INTO balance_entries(uniqueId, currency, balance) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE balance = balance_entries.balance + VALUES(balance);";
  }

  @Override
  void save() {
    try (Connection connection = dataSource.getConnection();
         PreparedStatement playerStatement = connection.prepareStatement(getSavePlayerStatement());
         PreparedStatement balanceStatement = connection.prepareStatement(getSaveBalanceStatement())) {

      for (PlayerAccount playerAccount : entries) {

        byte[] uniqueId = DataUtils.uuidToBytes(playerAccount.getUniqueId());

        if (playerAccount.isDirty()) {
          playerStatement.setBytes(1, uniqueId);
          playerStatement.setString(2, playerAccount.getUsername());
          playerStatement.addBatch();
        }

        for (BalanceEntry balanceEntry : playerAccount.getBalanceEntries()) {
          if (!balanceEntry.isDirty())
            continue;

          balanceStatement.setBytes(1, uniqueId);
          balanceStatement.setString(2, balanceEntry.getCurrency());
          balanceStatement.setDouble(3, balanceEntry.getUnsavedBalance());
          balanceStatement.addBatch();
          balanceEntry.markClean();
        }

        playerAccount.markClean();
      }

      playerStatement.executeBatch();
      balanceStatement.executeBatch();

      connection.commit();
    } catch (SQLException exception) {
      config.getPlugin().getLogger().log(Level.SEVERE, "Failed to save data to SQL database.", exception);
    }
  }

  @Override
  public void close() {
    super.close();
    dataSource.close();
  }

}
