package me.jeremiah.economy.config;

import me.jeremiah.economy.AbstractEconomyPlugin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.logging.Level;

abstract class AbstractConfig {

  private final @NotNull AbstractEconomyPlugin plugin;
  private final @NotNull File file;
  private final @NotNull YamlConfiguration config = new YamlConfiguration();

  AbstractConfig(@NotNull AbstractEconomyPlugin plugin, @NotNull String fileName) {
    this.plugin = plugin;
    this.file = new File(plugin.getDataFolder(), fileName);
  }

  public @NotNull AbstractEconomyPlugin getPlugin() {
    return plugin;
  }

  public @NotNull YamlConfiguration getConfig() {
    return config;
  }

  public void load() {
    createFile(plugin);
    update(plugin);
    loadFile(plugin);
  }

  private void createFile(@NotNull AbstractEconomyPlugin plugin) {
    if (!file.exists())
      plugin.saveResource(file.getName(), false);
  }

  private void loadFile(@NotNull AbstractEconomyPlugin plugin) {
    try {
      config.load(file);
    } catch (IOException exception) {
      plugin.getLogger().log(Level.SEVERE, "Failed to load " + file.getName(), exception);
    } catch (InvalidConfigurationException exception) {
      plugin.getLogger().log(Level.SEVERE, "Failed to load %s due to an invalid configuration".formatted(file.getName()), exception);
    }
  }

  private void update(@NotNull AbstractEconomyPlugin plugin) {
    try {
      boolean save = false;
      InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(plugin.getResource(file.getName()), "The developer of this plugin is a dumbass..."));
      YamlConfiguration internalConfig = YamlConfiguration.loadConfiguration(inputStreamReader);
      inputStreamReader.close();

      YamlConfiguration messages = YamlConfiguration.loadConfiguration(file);

      for (String key : internalConfig.getKeys(true))
        if (!messages.contains(key)) {
          messages.set(key, internalConfig.get(key));
          save = true;
        }

      if (save)
        messages.save(file);
    } catch (IOException exception) {
      plugin.getLogger().log(Level.SEVERE, "Failed to update " + file.getName(), exception);
    }
  }

}