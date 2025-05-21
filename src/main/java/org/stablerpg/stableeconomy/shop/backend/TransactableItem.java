package org.stablerpg.stableeconomy.shop.backend;

import io.papermc.paper.entity.PlayerGiveResult;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;
import org.stablerpg.stableeconomy.currency.Currency;
import org.stablerpg.stableeconomy.shop.exceptions.BuyException;
import org.stablerpg.stableeconomy.shop.exceptions.CannotBuyException;
import org.stablerpg.stableeconomy.shop.exceptions.NotEnoughSpaceException;
import org.stablerpg.stableeconomy.shop.gui.ItemFormatter;
import org.stablerpg.stableeconomy.shop.gui.Itemable;
import org.stablerpg.stableeconomy.shop.util.InventoryUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class TransactableItem implements Itemable {

  public static TransactableItem deserialize(EconomyPlatform platform, Currency currency, ConfigurationSection section, ItemFormatter itemFormatter) throws DeserializationException {
    ConfigurationSection itemSection = section.getConfigurationSection("item");

    ItemBuilder itemBuilder = ItemBuilder.deserialize(itemSection);
    int amount = section.getInt("amount", itemBuilder.amount());
    String displayName = section.getString("display-name");
    List<String> description = section.getStringList("description");
    itemFormatter = ItemFormatter.deserialize(section, itemFormatter);
    double buyPrice = section.getDouble("buy-price", -1);
    double sellValue = section.getDouble("sell-value", -1);
    if (buyPrice == -1) {
      buyPrice = platform.getPriceProvider().getBuyPrice(itemBuilder.build());
      if (buyPrice == -1)
        throw new DeserializationException("Failed to locate buy price in " + section.getName());
    }
    if (sellValue == -1)
      sellValue = platform.getPriceProvider().getSellValue(itemBuilder.build());
    return new TransactableItem(currency, itemBuilder, amount, displayName, description, itemFormatter, buyPrice, sellValue);
  }

  private final Currency currency;
  private final ItemBuilder itemBuilder;

  private final int amount;
  private final Component displayName;
  private final List<Component> description;
  private final ItemFormatter itemFormatter;

  private final double buyPrice;
  private final double sellValue;

  public TransactableItem(Currency currency, ItemBuilder itemBuilder, int amount, Component displayName, List<Component> description, ItemFormatter itemFormatter, double buyPrice, double sellValue) {
    this.currency = currency;
    this.itemBuilder = itemBuilder;
    this.amount = amount;
    this.displayName = displayName;
    this.description = Collections.unmodifiableList(description);
    this.itemFormatter = itemFormatter;
    this.buyPrice = buyPrice;
    this.sellValue = sellValue;
  }

  public TransactableItem(Currency currency, ItemBuilder itemBuilder, int amount, String displayName, List<String> description, ItemFormatter itemFormatter, double buyPrice, double sellValue) {
    this.currency = currency;
    this.itemBuilder = itemBuilder;
    this.amount = amount;
    this.displayName = MiniMessage.miniMessage().deserialize(itemFormatter.formatName(displayName));
    this.description = description.stream().map(itemFormatter::formatLore).map(MiniMessage.miniMessage()::deserialize).toList();
    this.itemFormatter = itemFormatter;
    this.buyPrice = buyPrice;
    this.sellValue = sellValue;
  }

  public void purchase(Player player) throws BuyException {
    ItemStack item = itemBuilder.build();
    item.setAmount(amount);

    double buyPrice = this.buyPrice;

    if (!currency.hasBalance(player, buyPrice))
      throw new CannotBuyException("Not enough money to buy item");

    if (!InventoryUtil.canFit(player, item))
      throw new NotEnoughSpaceException("Not enough space in inventory");

    currency.subtractBalance(player, buyPrice);
    PlayerGiveResult result = player.give(List.of(item), false);

    if (!result.leftovers().isEmpty())
      throw new BuyException("Failed to accurately detect available space in inventory");
  }

  public void sell(Player player) throws NotEnoughSpaceException {
    ItemStack item = itemBuilder.build();
    item.setAmount(amount);

    double sellValue = this.sellValue;

    if (!player.getInventory().containsAtLeast(item, amount))
      throw new NotEnoughSpaceException();

    currency.addBalance(player, sellValue);
    player.getInventory().removeItem(item);
  }

  @Override
  public ItemStack build() {
    return this.itemBuilder.copy(builder -> {
      List<Component> description = this.description != null ? new ArrayList<>(this.description) : new ArrayList<>();
      if (buyPrice != -1 || sellValue != -1)
        description.add(Component.empty());
      if (buyPrice != -1)
        description.add(MiniMessage.miniMessage().deserialize(itemFormatter.formatBuyPriceLore(currency.format(buyPrice))));
      if (sellValue != -1)
        description.add(MiniMessage.miniMessage().deserialize(itemFormatter.formatSellValueLore(currency.format(sellValue))));
      builder.displayName(displayName).lore(description).itemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DYE, ItemFlag.HIDE_UNBREAKABLE);
    }).build();
  }

}
