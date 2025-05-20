package org.stablerpg.stableeconomy.shop.backend;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.shop.ShopManager;
import org.stablerpg.stableeconomy.shop.gui.ShopCategoryViewTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

@Getter
public class ShopCategory {

  public static ShopCategory of(ShopManager manager, String id, ConfigurationSection section) {
    EconomyPlatform platform = manager.getPlatform();
    Logger logger = platform.getLogger();
    
    String rawTitle = section.getString("title");
    if (rawTitle == null) {
      logger.warning("Failed to locate title for " + id);
      return null;
    }
    Component title = MiniMessage.miniMessage().deserialize(rawTitle);

    int rows = section.getInt("rows", 3);
    ConfigurationSection backgroundItemSection = section.getConfigurationSection("background-item");
    ItemBuilder backgroundItem;
    if (backgroundItemSection == null) {
      logger.warning("Failed to locate background item section for " + id);
      backgroundItem = new ItemBuilder();
    } else
      backgroundItem = ItemBuilder.of(backgroundItemSection);
    int[] backgroundSlots = section.getIntegerList("background-slots").stream().mapToInt(Integer::intValue).toArray();
    ShopCategoryViewTemplate template = new ShopCategoryViewTemplate(rows, backgroundItem, backgroundSlots);

    ShopCategory category = new ShopCategory(manager, title, template);

    ConfigurationSection itemsSection = section.getConfigurationSection("items");
    if (itemsSection == null) {
      logger.warning("Failed to locate items section for " + id);
      return category;
    }

    itemsSection.getKeys(false).stream()
      .map(itemsSection::getConfigurationSection)
      .filter(Objects::nonNull)
      .forEach(itemSection -> {
        int slot = Integer.parseInt(itemSection.getName());
        if (itemSection.isConfigurationSection("item"))
          category.addTransactableItem(slot, TransactableItem.of(platform, id, itemSection));
        else
          category.addShopItem(slot, ShopItem.of(manager, id, itemSection));
      });

    return category;
  }
  
  private final ShopManager manager;

  private final Component title;

  private final ShopCategoryViewTemplate context;
  private final Map<Integer, TransactableItem> transactableItems = new HashMap<>();
  private final Map<Integer, ShopItem> shopItems = new HashMap<>();

  public ShopCategory(ShopManager manager, Component title, ShopCategoryViewTemplate context) {
    this.manager = manager;
    this.title = title;
    this.context = context;
  }

  public void addTransactableItem(Integer slot, TransactableItem item) {
    transactableItems.put(slot, item);
  }

  public void addShopItem(Integer slot, ShopItem item) {
    shopItems.put(slot, item);
  }

}
