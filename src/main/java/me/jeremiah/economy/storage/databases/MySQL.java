package me.jeremiah.economy.storage.databases;

import me.jeremiah.economy.config.BasicConfig;
import me.jeremiah.economy.storage.DatabaseInfo;
import org.jetbrains.annotations.NotNull;

public final class MySQL extends AbstractSQLDatabase {

  public MySQL(@NotNull BasicConfig config) {
    super(com.mysql.cj.jdbc.Driver.class, config);
  }

  @Override
  void processConfig(@NotNull DatabaseInfo databaseInfo) {
    hikariConfig.setJdbcUrl("jdbc:mysql://%s/%s".formatted(databaseInfo.getUrl(), databaseInfo.getName()));
    hikariConfig.setUsername(databaseInfo.getUsername());
    hikariConfig.setPassword(databaseInfo.getPassword());
  }

}
