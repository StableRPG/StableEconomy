package me.jeremiah.economy.data.databases;

import com.zaxxer.hikari.HikariConfig;
import me.jeremiah.economy.EconomyPlatform;
import me.jeremiah.economy.data.util.DatabaseInfo;
import org.jetbrains.annotations.NotNull;

public final class MySQL extends AbstractSQLDatabase {

  public MySQL(@NotNull EconomyPlatform platform) {
    super(com.mysql.cj.jdbc.Driver.class, platform);
  }

  @Override
  void processConfig(@NotNull HikariConfig hikariConfig, @NotNull DatabaseInfo databaseInfo) {
    hikariConfig.setJdbcUrl("jdbc:mysql://%s/%s".formatted(databaseInfo.getUrl(), databaseInfo.getName()));
    hikariConfig.setUsername(databaseInfo.getUsername());
    hikariConfig.setPassword(databaseInfo.getPassword());
  }

}
