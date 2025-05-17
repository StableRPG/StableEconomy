package org.stablerpg.stableeconomy.config.shop;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.shop.ShopManager;
import org.stablerpg.stableeconomy.shop.backend.ItemBuilder;
import org.stablerpg.stableeconomy.shop.backend.ShopCategory;
import org.stablerpg.stableeconomy.shop.backend.ShopItem;
import org.stablerpg.stableeconomy.shop.backend.ShopItemAction;
import org.stablerpg.stableeconomy.shop.backend.TransactableItem;
import org.stablerpg.stableeconomy.shop.frontend.ShopCategoryViewTemplate;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class ShopConfigImpl implements ShopConfig {

  private final @NotNull EconomyPlatform platform;

  private final @NotNull File shopConfig;
  private final @NotNull File shopDir;

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
        platform.getLogger().warning("Failed to create shops directory");
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

    File[] shopFiles = shopDir.listFiles(file -> file.getName().endsWith(".yml"));

    if (shopFiles == null) {
      platform.getLogger().warning("Failed to load shops directory");
      return;
    }

    for (File shopFile : shopFiles) {
      YamlConfiguration config = YamlConfiguration.loadConfiguration(shopFile);
      ConfigurationSection categorySection = config.getConfigurationSection("category");
      if (categorySection == null) {
        platform.getLogger().warning("Failed to locate category section for " + shopFile.getName());
        continue;
      }

      String id = shopFile.getName().replaceAll("\\.(yml|yaml)", "");
      System.out.println("Loading shop category " + id);

      String rawTitle = categorySection.getString("title");
      if (rawTitle == null) {
        platform.getLogger().warning("Failed to locate title for " + shopFile.getName());
        continue;
      }
      Component title = MiniMessage.miniMessage().deserialize(rawTitle);

      int rows = categorySection.getInt("rows", 3);
      ConfigurationSection backgroundItemSection = categorySection.getConfigurationSection("background-item");
      ItemBuilder backgroundItem;
      if (backgroundItemSection == null) {
        platform.getLogger().warning("Failed to locate background item section for " + shopFile.getName());
        backgroundItem = new ItemBuilder();
      } else
        backgroundItem = ItemBuilder.of(backgroundItemSection);
      int[] backgroundSlots = categorySection.getIntegerList("background-slots").stream().mapToInt(Integer::intValue).toArray();
      ShopCategoryViewTemplate template = new ShopCategoryViewTemplate(rows, backgroundItem, backgroundSlots);

      ShopCategory category = new ShopCategory(shopManager, id, title, template);
      shopManager.addCategory(category);
      if (shopFile.getName().equals("main.yml"))
        shopManager.setMainCategory(category);

      ConfigurationSection itemsSection = categorySection.getConfigurationSection("items");
      if (itemsSection == null) {
        platform.getLogger().warning("Failed to locate items section for " + shopFile.getName());
        continue;
      }

      itemsSection.getKeys(false).stream()
        .map(itemsSection::getConfigurationSection)
        .filter(Objects::nonNull)
        .forEach(itemSection -> {
          int slot = Integer.parseInt(itemSection.getName());
          ConfigurationSection itemBuilderSection = itemSection.getConfigurationSection("item");
          if (itemBuilderSection != null) { // Purchasable item
            ItemBuilder itemBuilder = ItemBuilder.of(itemBuilderSection);
            if (itemBuilder == null) {
              platform.getLogger().warning("Failed to load item builder for " + itemSection.getName() + " in " + shopFile.getName());
              return;
            }
            int amount = itemSection.getInt("amount", itemBuilder.amount());
            String displayName = itemSection.getString("display-name");
            List<String> description = itemSection.getStringList("description");
            double buyPrice = itemSection.getDouble("buy-price", -1);
            double sellValue = itemSection.getDouble("sell-value", -1);
            if (buyPrice == -1) {
              double priceProviderBuyPrice = platform.getPriceProvider().getBuyPrice(itemBuilder.build());
              if (priceProviderBuyPrice == -1)
                platform.getLogger().warning("Failed to locate buy price for " + itemBuilderSection.getName() + " in " + shopFile.getName());
              buyPrice = priceProviderBuyPrice;
            }
            if (sellValue == -1) {
              double priceProviderSellValue = platform.getPriceProvider().getSellValue(itemBuilder.build());
              if (priceProviderSellValue == -1)
                platform.getLogger().warning("Failed to locate sell value for " + itemBuilderSection.getName() + " in " + shopFile.getName());
              sellValue = priceProviderSellValue;
            }
            category.addTransactableItem(slot, new TransactableItem(platform, itemBuilder, amount, displayName, description, buyPrice, sellValue));
          } else { // Shop Item
            ItemBuilder itemBuilder = ItemBuilder.of(itemSection);
            String rawActionType = itemSection.getString("action");
            if (rawActionType == null) {
              platform.getLogger().warning("Failed to locate action type for " + itemSection.getName() + " in " + shopFile.getName());
              return;
            }
            rawActionType = rawActionType.toUpperCase();
            ShopItemAction action = ShopItemAction.valueOf(rawActionType);
            String[] actionArgs = switch (action) {
              case OPEN_CATEGORY -> new String[]{itemSection.getString("category")};
              default -> null;
            };
            category.addShopItem(slot, new ShopItem(platform.getShopManager(), itemBuilder, action, actionArgs));
          }
        });
    }

    shopManager.load();
  }

  @Override
  public @NotNull Logger getLogger() {
    return platform.getLogger();
  }

  @Override
  public void close() {
    shopManager.close();
  }

}
