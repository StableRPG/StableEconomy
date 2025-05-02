package me.jeremiah.stableeconomy.config;

import me.jeremiah.stableeconomy.AbstractEconomyPlugin;
import me.jeremiah.stableeconomy.data.util.DatabaseInfo;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public final class Config extends AbstractConfig implements BasicConfig {

  private DatabaseInfo databaseInfo;

  public Config(@NotNull AbstractEconomyPlugin plugin) {
    super(plugin,"config.yml");
  }

  @Override
  public void load() {
    super.load();

    databaseInfo = new DatabaseInfo(
      getConfig().getString("database.type", "h2"),
      getConfig().getString("database.address", "localhost"),
      getConfig().getInt("database.port", 3306),
      getConfig().getString("database.name", "economy"),
      getConfig().getString("database.username", "root"),
      getConfig().getString("database.password", "root")
    );

    databaseInfo.setAutoSaveInterval(getConfig().getLong("database.auto-save-interval", 300));
  }

  @Override
  public @NotNull Logger getLogger() {
    return getPlugin().getLogger();
  }

  public @NotNull DatabaseInfo getDatabaseInfo() {
    return databaseInfo;
  }

}
