package me.jeremiah.stableeconomy.data.databases;

import com.zaxxer.hikari.HikariConfig;
import me.jeremiah.stableeconomy.EconomyPlatform;
import me.jeremiah.stableeconomy.data.util.DatabaseInfo;
import org.jetbrains.annotations.NotNull;

public final class SQLite extends AbstractSQLDatabase {

  public SQLite(@NotNull EconomyPlatform platform) {
    super(org.sqlite.JDBC.class, platform);
  }

  @Override
  void processConfig(@NotNull HikariConfig hikariConfig, @NotNull DatabaseInfo databaseInfo) {
    hikariConfig.setJdbcUrl("jdbc:sqlite:./plugins/Economy/%s".formatted(databaseInfo.getName()));
  }

  @Override
  String getSavePlayerStatement() {
    return "INSERT INTO player_entries(uniqueId, username) VALUES (?, ?) ON CONFLICT(uniqueId) DO UPDATE SET username = excluded.username";
  }

  @Override
  String getSaveBalanceStatement() {
    return "INSERT INTO balance_entries(uniqueId, currency, balance) VALUES (?, ?, ?) ON CONFLICT(uniqueId, currency) DO UPDATE SET balance = balance_entries.balance + excluded.balance";
  }

}
