package me.jeremiah.economy.config;

import me.jeremiah.economy.AbstractEconomyPlugin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

  protected void createFile(@NotNull AbstractEconomyPlugin plugin) {
    if (!file.exists())
      plugin.saveResource(file.getName(), false);
  }

  protected void loadFile(@NotNull AbstractEconomyPlugin plugin) {
    try {
      config.load(file);
    } catch (IOException exception) {
      plugin.getLogger().log(Level.SEVERE, "Failed to load %s".formatted(file.getName()), exception);
    } catch (InvalidConfigurationException exception) {
      plugin.getLogger().log(Level.SEVERE, "Failed to load %s due to an invalid configuration".formatted(file.getName()), exception);
    }
  }

  protected void update(@NotNull AbstractEconomyPlugin plugin) {
    boolean save = false;
    YamlConfiguration internalMessages;
    try (InputStream inputStream = Objects.requireNonNull(plugin.getResource(file.getName()), "The developer of this plugin is a dumbass...");
         InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
      internalMessages = YamlConfiguration.loadConfiguration(inputStreamReader);
    } catch (IOException exception) {
      plugin.getLogger().log(Level.SEVERE, "Failed to load " + file.getName(), exception);
      return;
    }

    YamlConfiguration messages = YamlConfiguration.loadConfiguration(file);

    for (String key : internalMessages.getKeys(true))
      if (!messages.contains(key)) {
        messages.set(key, internalMessages.get(key));
        save = true;
      }

    for (String key : messages.getKeys(true)) {
      if (!internalMessages.contains(key)) {
        messages.set(key, null);
        save = true;
      }

      if (save)
        try {
          messages.save(file);
        } catch (IOException exception) {
          plugin.getLogger().log(Level.SEVERE, "Failed to update " + file.getName(), exception);
        }
    }
  }

}