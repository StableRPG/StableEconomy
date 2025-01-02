package me.jeremiah.economy.data.databases;

import com.zaxxer.hikari.HikariConfig;
import me.jeremiah.economy.config.BasicConfig;
import me.jeremiah.economy.data.util.DatabaseInfo;
import org.jetbrains.annotations.NotNull;

public final class PostgreSQL extends AbstractSQLDatabase {

  PostgreSQL(@NotNull BasicConfig config) {
    super(org.postgresql.Driver.class, config);
  }

  @Override
  void processConfig(@NotNull HikariConfig hikariConfig, @NotNull DatabaseInfo databaseInfo) {
    hikariConfig.setJdbcUrl("jdbc:postgresql://%s/%s".formatted(databaseInfo.getUrl(), databaseInfo.getName()));
    hikariConfig.setUsername(databaseInfo.getUsername());
    hikariConfig.setPassword(databaseInfo.getPassword());
  }

  @Override
  String getSavePlayerStatement() {
    return "INSERT INTO player_entries(uniqueId, username) VALUES(?, ?) ON CONFLICT(uniqueId) DO UPDATE SET username = EXCLUDED.username;";
  }

  @Override
  String getSaveBalanceStatement() {
    return "INSERT INTO balance_entries(uniqueId, currency, balance) VALUES(?, ?, ?) ON CONFLICT(uniqueId, currency) DO UPDATE SET balance = balance_entries.balance + EXCLUDED.balance;";
  }

}
