package me.jeremiah.economy.storage.databases;

import com.zaxxer.hikari.HikariConfig;
import me.jeremiah.economy.config.BasicConfig;
import me.jeremiah.economy.storage.DatabaseInfo;
import org.jetbrains.annotations.NotNull;

public final class H2 extends AbstractSQLDatabase {

  public H2(@NotNull BasicConfig config) {
    super(org.h2.Driver.class, config);
  }

  @Override
  void processConfig(@NotNull HikariConfig hikariConfig, @NotNull DatabaseInfo databaseInfo) {
    hikariConfig.setJdbcUrl("jdbc:h2:./plugins/Economy/%s;MODE=MariaDB;DATABASE_TO_UPPER=FALSE".formatted(databaseInfo.getName()));
  }

}
