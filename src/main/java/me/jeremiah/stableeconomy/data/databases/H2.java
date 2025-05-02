package me.jeremiah.stableeconomy.data.databases;

import com.zaxxer.hikari.HikariConfig;
import me.jeremiah.stableeconomy.EconomyPlatform;
import me.jeremiah.stableeconomy.data.util.DatabaseInfo;
import org.jetbrains.annotations.NotNull;

public final class H2 extends AbstractSQLDatabase {

  public H2(@NotNull EconomyPlatform platform) {
    super(org.h2.Driver.class, platform);
  }

  @Override
  void processConfig(@NotNull HikariConfig hikariConfig, @NotNull DatabaseInfo databaseInfo) {
    hikariConfig.setJdbcUrl("jdbc:h2:./plugins/Economy/%s;MODE=MariaDB;DATABASE_TO_UPPER=FALSE".formatted(databaseInfo.getName()));
  }

}
