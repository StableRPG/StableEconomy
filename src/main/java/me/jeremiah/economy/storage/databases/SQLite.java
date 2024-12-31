package me.jeremiah.economy.storage.databases;

import me.jeremiah.economy.config.BasicConfig;
import me.jeremiah.economy.storage.DatabaseInfo;
import org.jetbrains.annotations.NotNull;

public final class SQLite extends AbstractSQLDatabase {

  SQLite(@NotNull BasicConfig config) {
    super(org.sqlite.JDBC.class, config);
  }

  @Override
  void processConfig(@NotNull DatabaseInfo databaseInfo) {
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
