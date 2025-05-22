package org.stablerpg.stableeconomy.shop.backend;

import dev.triumphteam.gui.click.ClickContext;
import dev.triumphteam.gui.click.GuiClick;
import io.papermc.paper.entity.PlayerGiveResult;
import lombok.Getter;
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
import org.stablerpg.stableeconomy.shop.exceptions.NotEnoughToSellException;
import org.stablerpg.stableeconomy.shop.gui.AbstractGuiItem;
import org.stablerpg.stableeconomy.shop.gui.ItemFormatter;
import org.stablerpg.stableeconomy.shop.util.InventoryUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class TransactableItem implements AbstractGuiItem {

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
        throw new DeserializationException("Failed to locate buy price for \"%s\"".formatted(section.getName()));
    }
    if (sellValue == -1)
      sellValue = platform.getPriceProvider().getSellValue(itemBuilder.build());
    return new TransactableItem(currency, itemBuilder, amount, displayName, description, itemFormatter, buyPrice, sellValue);
  }

  private final Currency currency;
  private final ItemBuilder itemBuilder;

  private final int amount;
  private final String displayName;
  private final List<String> description;
  private final ItemFormatter itemFormatter;

  private final double buyPrice;
  private final double sellValue;

  public TransactableItem(Currency currency, ItemBuilder itemBuilder, int amount, String displayName, List<String> description, ItemFormatter itemFormatter, double buyPrice, double sellValue) {
    this.currency = currency;
    this.itemBuilder = itemBuilder;
    this.amount = amount;
    this.displayName = displayName;
    this.description = Collections.unmodifiableList(description);
    this.itemFormatter = itemFormatter;
    this.buyPrice = buyPrice;
    this.sellValue = sellValue;
  }

  @Override
  public void execute(Player player, ClickContext context) {
    GuiClick clickType = context.guiClick();
    switch (clickType) {
      case LEFT, SHIFT_LEFT -> {
        try {
          purchase(player);
        } catch (CannotBuyException e) {
          player.sendRichMessage("<red>Not enough money to buy item!</red>");
        } catch (NotEnoughSpaceException e) {
          player.sendRichMessage("<red>Not enough space in inventory!</red>");
        } catch (BuyException e) {
          player.sendRichMessage("<red>Failed to accurately detect available space in inventory!</red>");
        }
      }
      case RIGHT, SHIFT_RIGHT -> {
        try {
          sell(player);
        } catch (NotEnoughToSellException e) {
          player.sendRichMessage("<red>You don't have the item to sell!</red>");
        }
      }
    }
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

  public void sell(Player player) throws NotEnoughToSellException {
    ItemStack item = itemBuilder.build();
    item.setAmount(amount);

    double sellValue = this.sellValue;

    if (!player.getInventory().containsAtLeast(item, amount))
      throw new NotEnoughToSellException();

    currency.addBalance(player, sellValue);
    player.getInventory().removeItem(item);
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
      } else
        description = new ArrayList<>();
      if (buyPrice != -1 || sellValue != -1)
        description.add(" ");
      if (buyPrice != -1)
        description.add(itemFormatter.formatBuyPriceLore(currency.format(buyPrice)));
      if (sellValue != -1)
        description.add(itemFormatter.formatSellValueLore(currency.format(sellValue)));
      builder.lore(description).itemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DYE, ItemFlag.HIDE_UNBREAKABLE);
    }).build();
  }

}
