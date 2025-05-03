package org.stablerpg.stableeconomy.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.AbstractEconomyPlugin;
import org.stablerpg.stableeconomy.data.util.DatabaseInfo;

import java.util.logging.Logger;

public final class Config extends AbstractConfig implements BasicConfig {

  private DatabaseInfo databaseInfo;

  public Config(@NotNull AbstractEconomyPlugin plugin) {
    super(plugin,"config.yml");
  }

  @Override
  public void load() {
    super.load();

    YamlConfiguration config = getConfig();

    databaseInfo = new DatabaseInfo(
      config.getString("database.type", "h2"),
      config.getString("database.address", "localhost"),
      config.getInt("database.port", 3306),
      config.getString("database.name", "economy"),
      config.getString("database.username", "root"),
      config.getString("database.password", "root")
    );

    databaseInfo.setAutoSaveInterval(config.getLong("database.auto-save-interval", 300));
  }

  @Override
  public @NotNull DatabaseInfo getDatabaseInfo() {
    return databaseInfo;
  }

  @Override
  public @NotNull Logger getLogger() {
    return getPlugin().getLogger();
  }

}
