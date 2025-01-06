package me.jeremiah.economy.data.databases;

import com.zaxxer.hikari.HikariConfig;
import me.jeremiah.economy.EconomyPlatform;
import me.jeremiah.economy.data.util.DatabaseInfo;
import org.jetbrains.annotations.NotNull;

public final class MariaDB extends AbstractSQLDatabase {

  public MariaDB(@NotNull EconomyPlatform platform) {
    super(org.mariadb.jdbc.Driver.class, platform);
  }

  @Override
  void processConfig(@NotNull HikariConfig hikariConfig, @NotNull DatabaseInfo databaseInfo) {
    hikariConfig.setJdbcUrl("jdbc:mariadb://%s/%s".formatted(databaseInfo.getUrl(), databaseInfo.getName()));
    hikariConfig.setUsername(databaseInfo.getUsername());
    hikariConfig.setPassword(databaseInfo.getPassword());
  }

}
