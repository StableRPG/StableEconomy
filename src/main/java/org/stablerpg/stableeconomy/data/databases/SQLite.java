package org.stablerpg.stableeconomy.data.databases;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.data.util.DatabaseInfo;

public final class SQLite extends AbstractSQLDatabase {

  public SQLite(@NotNull EconomyPlatform platform) {
    super(org.sqlite.JDBC.class, platform);
  }

  @Override
  void processConfig(@NotNull HikariConfig hikariConfig, @NotNull DatabaseInfo databaseInfo) {
    hikariConfig.setJdbcUrl("jdbc:sqlite:%s".formatted(databaseInfo.getFullPath()));
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
