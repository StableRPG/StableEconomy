package org.stablerpg.stableeconomy.shop.backend;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;
import org.stablerpg.stableeconomy.currency.Currency;
import org.stablerpg.stableeconomy.shop.ShopManager;
import org.stablerpg.stableeconomy.shop.gui.AbstractGuiItem;
import org.stablerpg.stableeconomy.shop.gui.ItemFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class ShopCategory {

  public static ShopCategory deserialize(ShopManager manager, ConfigurationSection section, ItemFormatter itemFormatter) throws DeserializationException {
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

    int rows = section.getInt("rows", 3);

    ConfigurationSection backgroundItemSection = section.getConfigurationSection("background-item");
    if (backgroundItemSection == null)
      throw new DeserializationException("Failed to locate background item section");
    ItemBuilder backgroundItem = ItemBuilder.deserialize(backgroundItemSection);

    int[] backgroundSlots = section.getIntegerList("background-slots").stream().mapToInt(Integer::intValue).toArray();

    itemFormatter = ItemFormatter.deserialize(section, itemFormatter);

    ShopCategory category = new ShopCategory(manager, title, rows, backgroundItem, backgroundSlots);

    ConfigurationSection itemsSection = section.getConfigurationSection("items");
    if (itemsSection == null)
      throw new DeserializationException("Failed to locate items section");

    for (String key : itemsSection.getKeys(false)) {
      ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);

      int slot = Integer.parseInt(itemSection.getName());

      if (itemSection.isConfigurationSection("item"))
        category.addGuiItem(slot, TransactableItem.deserialize(platform, currency, itemSection, itemFormatter));
      else
        category.addGuiItem(slot, ShopItem.deserialize(manager, itemSection, itemFormatter));
    }

    return category;
  }

  private final ShopManager manager;

  private final Component title;
  private final int rows;
  private final ItemBuilder background;
  private final int[] backgroundSlots;

  private final Map<Integer, AbstractGuiItem> items = new HashMap<>();

  public ShopCategory(ShopManager manager, Component title, int rows, ItemBuilder background, int[] backgroundSlots) {
    this.manager = manager;
    this.title = title;
    this.rows = rows;
    this.background = background;
    this.backgroundSlots = backgroundSlots;
  }

  public void addGuiItem(Integer slot, AbstractGuiItem item) {
    items.put(slot, item);
  }

}
