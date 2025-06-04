package org.stablerpg.stableeconomy.shop.backend;

import dev.triumphteam.gui.click.ClickContext;
import dev.triumphteam.gui.click.GuiClick;
import io.papermc.paper.entity.PlayerGiveResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;
import org.stablerpg.stableeconomy.config.shop.ShopLocale;
import org.stablerpg.stableeconomy.config.shop.ShopMessageType;
import org.stablerpg.stableeconomy.currency.Currency;
import org.stablerpg.stableeconomy.shop.gui.AbstractGuiItem;
import org.stablerpg.stableeconomy.shop.gui.ItemFormatter;
import org.stablerpg.stableeconomy.shop.util.InventoryUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record TransactableItem(Currency currency, ItemBuilder itemBuilder, int amount, String displayName, List<String> description,
                               ItemFormatter itemFormatter, double buyPrice, double sellValue, ShopLocale locale) implements AbstractGuiItem {

  public static TransactableItem deserialize(EconomyPlatform platform, Currency currency, ConfigurationSection section, ItemFormatter itemFormatter, ShopLocale locale) throws DeserializationException {
    ConfigurationSection itemSection = section.getConfigurationSection("item");

    if (itemSection == null)
      throw new DeserializationException("Failed to locate item section in \"%s\"".formatted(section.getName()));

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
        throw new DeserializationException("Failed to locate buy price for \"%s\"".formatted(section.getName()));
    }
    if (sellValue == -1) sellValue = platform.getPriceProvider().getSellValue(itemBuilder.build());
    return new TransactableItem(currency, itemBuilder, amount, displayName, description, itemFormatter, buyPrice, sellValue, locale);
  }

  public TransactableItem(Currency currency, ItemBuilder itemBuilder, int amount, String displayName, List<String> description, ItemFormatter itemFormatter, double buyPrice, double sellValue, ShopLocale locale) {
    this.currency = currency;
    this.itemBuilder = itemBuilder;
    this.amount = amount;
    this.displayName = displayName;
    this.description = Collections.unmodifiableList(description);
    this.itemFormatter = itemFormatter;
    this.buyPrice = buyPrice;
    this.sellValue = sellValue;
    this.locale = locale;
  }

  @Override
  public void execute(Player player, ClickContext context) {
    GuiClick clickType = context.guiClick();
    switch (clickType) {
      case LEFT, SHIFT_LEFT -> purchase(player);
      case RIGHT, SHIFT_RIGHT -> sell(player);
    }
  }

  public void purchase(Player player) {
    ItemStack item = itemBuilder.build();
    item.setAmount(amount);

    double buyPrice = this.buyPrice;

    if (!currency.hasBalance(player, buyPrice)) {
      locale.sendParsedMessage(player, ShopMessageType.INSUFFICIENT_BALANCE, "required-balance", currency.format(buyPrice), "balance", currency.format(currency.getBalance(player)), "amount", String.valueOf(amount), "item-name", displayName);
      return;
    }

    if (!InventoryUtil.canFit(player, item)) {
      locale.sendParsedMessage(player, ShopMessageType.NOT_ENOUGH_SPACE, "amount", String.valueOf(amount), "item-name", displayName);
      return;
    }

    currency.subtractBalance(player, buyPrice);
    PlayerGiveResult result = player.give(List.of(item), false);

    if (!result.leftovers().isEmpty())
      player.sendRichMessage("<red>Failed to accurately detect available space in inventory!</red>");

    locale.sendParsedMessage(player, ShopMessageType.SUCCESSFULLY_PURCHASED, "payed-amount", currency.format(buyPrice), "amount", String.valueOf(amount), "item-name", displayName);
  }

  public void sell(Player player) {
    ItemStack item = itemBuilder.build();
    item.setAmount(amount);

    double sellValue = this.sellValue;

    if (!player.getInventory().containsAtLeast(item, amount)) {
      locale.sendParsedMessage(player, ShopMessageType.NOT_ENOUGH_TO_SELL, "amount", String.valueOf(amount), "item-name", displayName);
      return;
    }

    currency.addBalance(player, sellValue);
    player.getInventory().removeItem(item);

    locale.sendParsedMessage(player, ShopMessageType.SUCCESSFULLY_SOLD, "received-amount", currency.format(sellValue), "amount", String.valueOf(amount), "item-name", displayName);
  }

  @Override
  public ItemStack build(Player player) {
    return this.itemBuilder.copy(builder -> {
      if (displayName != null) {
        String displayName = itemFormatter.format(this.displayName, itemFormatter::formatName, player);
        builder.displayName(itemFormatter.formatName(displayName));
      }
      List<String> description;
      if (this.description != null) {
        description = new ArrayList<>(this.description);
        description = itemFormatter.format(description, itemFormatter::formatLore, player);
      } else description = new ArrayList<>();
      if (buyPrice != -1 || sellValue != -1) description.add(" ");
      if (buyPrice != -1) description.add(itemFormatter.formatBuyPriceLore(currency.format(buyPrice)));
      if (sellValue != -1) description.add(itemFormatter.formatSellValueLore(currency.format(sellValue)));
      builder.lore(description).itemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DYE, ItemFlag.HIDE_UNBREAKABLE);
    }).build();
  }

}
