package org.stablerpg.stableeconomy.config.shop;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;
import org.stablerpg.stableeconomy.shop.ShopCommand;
import org.stablerpg.stableeconomy.shop.ShopManager;
import org.stablerpg.stableeconomy.shop.backend.ShopCategory;
import org.stablerpg.stableeconomy.shop.gui.ItemFormatter;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

public class ShopConfigImpl implements ShopConfig {

  private final @NotNull EconomyPlatform platform;

  private final @NotNull File shopConfig;
  private final @NotNull File shopDir;

  private final Collection<ShopCommand> shopCommands = new HashSet<>();
  @Getter
  private final ShopManager shopManager;

  public ShopConfigImpl(@NotNull EconomyPlatform platform) {
    this.platform = platform;
    this.shopConfig = new File(platform.getPlugin().getDataFolder(), "shops.yml");
    this.shopDir = new File(platform.getPlugin().getDataFolder(), "shops");
    this.shopManager = new ShopManager(platform);
  }

  @Override
  public void load() {
    if (!shopConfig.exists())
      platform.getPlugin().saveResource("shops.yml", false);
    if (!shopDir.exists()) {
      if (!shopDir.mkdir()) {
        getLogger().warning("Failed to create shops directory");
        return;
      }
      platform.getPlugin().saveResource("shops/main.yml", false);
      platform.getPlugin().saveResource("shops/blocks-1.yml", false);
      platform.getPlugin().saveResource("shops/blocks-2.yml", false);
      platform.getPlugin().saveResource("shops/decorations-1.yml", false);
      platform.getPlugin().saveResource("shops/decorations-2.yml", false);
      platform.getPlugin().saveResource("shops/miscellaneous-1.yml", false);
      platform.getPlugin().saveResource("shops/miscellaneous-2.yml", false);
    }

    YamlConfiguration config = YamlConfiguration.loadConfiguration(shopConfig);

    ItemFormatter defaultLoreTemplate = ItemFormatter.deserialize(config);

    ConfigurationSection commandsSection = config.getConfigurationSection("shop-commands");
    if (commandsSection == null) {
      getLogger().warning("Failed to locate shop-commands section");
      return;
    }
    for (String commandName : commandsSection.getKeys(false)) {
      ConfigurationSection commandSection = commandsSection.getConfigurationSection(commandName);
      ShopCommand command;
      try {
        command = ShopCommand.deserialize(shopManager, commandSection);
      } catch (DeserializationException e) {
        getLogger().warning("Failed to deserialize command \"%s\": %s".formatted(commandName, e.getMessage()));
        continue;
      }
      shopCommands.add(command);
    }

    File[] shopFiles = shopDir.listFiles(file -> file.getName().endsWith(".yml"));

    if (shopFiles == null) {
      getLogger().warning("Failed to load shops directory");
      return;
    }

    for (File shopFile : shopFiles) {
      YamlConfiguration categoryConfig = YamlConfiguration.loadConfiguration(shopFile);
      ConfigurationSection categorySection = categoryConfig.getConfigurationSection("category");
      if (categorySection == null) {
        getLogger().warning("Failed to locate category section for " + shopFile.getName());
        continue;
      }

      String id = shopFile.getName().replaceAll("\\.yml", "");
      ShopCategory category;
      try {
        category = ShopCategory.deserialize(shopManager, categorySection, defaultLoreTemplate);
      } catch (DeserializationException e) {
        getLogger().warning("Failed to deserialize category \"%s\": %s".formatted(id, e.getMessage()));
        continue;
      }
      shopManager.addCategory(id, category);
    }

    shopManager.load();

    for (ShopCommand command : shopCommands)
      command.register();
  }

  @Override
  public void close() {
    for (ShopCommand command : shopCommands)
      command.unregister();
    shopCommands.clear();
    shopManager.close();
  }

  @Override
  public @NotNull Logger getLogger() {
    return platform.getLogger();
  }

}
