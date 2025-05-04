package org.stablerpg.stableeconomy.config.database;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.AbstractEconomyPlugin;
import org.stablerpg.stableeconomy.config.AbstractConfig;
import org.stablerpg.stableeconomy.data.util.DatabaseInfo;

import java.util.logging.Logger;

public class DatabaseConfigImpl extends AbstractConfig implements DatabaseConfig {

  private DatabaseInfo databaseInfo;

  public DatabaseConfigImpl(@NotNull AbstractEconomyPlugin plugin) {
    super(plugin, "database.yml");
  }

  @Override
  public @NotNull DatabaseInfo getDatabaseInfo() {
    return databaseInfo;
  }

  @Override
  public @NotNull Logger getLogger() {
    return getPlugin().getLogger();
  }

  @Override
  public void load() {
    load0();

    YamlConfiguration config = getConfig();

    databaseInfo = new DatabaseInfo(config.getString("database.type", "h2"), config.getString("database.address", "localhost"), config.getInt("database.port", 3306), config.getString("database.name", "economy"), config.getString("database.username", "root"), config.getString("database.password", "root"));

    databaseInfo.setAutoSaveInterval(config.getLong("database.auto-save-interval", 300));
  }

}
