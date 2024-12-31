package me.jeremiah.economy.storage.databases;

import me.jeremiah.economy.config.BasicConfig;
import me.jeremiah.economy.storage.DatabaseInfo;
import org.jetbrains.annotations.NotNull;

public final class MariaDB extends AbstractSQLDatabase {

  public MariaDB(@NotNull BasicConfig config) {
    super(org.mariadb.jdbc.Driver.class, config);
  }

  @Override
  void processConfig(@NotNull DatabaseInfo databaseInfo) {
    hikariConfig.setJdbcUrl("jdbc:mariadb://%s/%s".formatted(databaseInfo.getUrl(), databaseInfo.getName()));
    hikariConfig.setUsername(databaseInfo.getUsername());
    hikariConfig.setPassword(databaseInfo.getPassword());
  }

}
