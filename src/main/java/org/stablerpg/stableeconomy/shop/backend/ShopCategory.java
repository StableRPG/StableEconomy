package org.stablerpg.stableeconomy.shop.backend;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;
import org.stablerpg.stableeconomy.currency.Currency;
import org.stablerpg.stableeconomy.shop.ShopManager;
import org.stablerpg.stableeconomy.shop.gui.ShopCategoryViewTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class ShopCategory {

  public static ShopCategory deserialize(ShopManager manager, ConfigurationSection section) throws DeserializationException {
    EconomyPlatform platform = manager.getPlatform();

    String rawCurrency = section.getString("currency", "default");
    Optional<Currency> optionalCurrency = platform.getCurrency(rawCurrency);
    if (optionalCurrency.isEmpty())
      throw new DeserializationException("Failed to locate currency " + rawCurrency);
    Currency currency = optionalCurrency.get();

    String rawTitle = section.getString("title");
    if (rawTitle == null)
      throw new DeserializationException("Failed to locate title");
    Component title = MiniMessage.miniMessage().deserialize(rawTitle);

    ShopCategoryViewTemplate template = ShopCategoryViewTemplate.deserialize(section);
    ShopCategory category = new ShopCategory(manager, title, template);

    ConfigurationSection itemsSection = section.getConfigurationSection("items");
    if (itemsSection == null)
      throw new DeserializationException("Failed to locate items section");

    for (String key : itemsSection.getKeys(false)) {
      ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);

      int slot = Integer.parseInt(itemSection.getName());

      if (itemSection.isConfigurationSection("item"))
        category.addTransactableItem(slot, TransactableItem.deserialize(platform, currency, itemSection));
      else
        category.addShopItem(slot, ShopItem.deserialize(manager, itemSection));
    }

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
