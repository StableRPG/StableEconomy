package org.stablerpg.stableeconomy.shop.backend;

import io.papermc.paper.entity.PlayerGiveResult;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.stablerpg.stableeconomy.api.EconomyAPI;
import org.stablerpg.stableeconomy.shop.exceptions.BuyException;
import org.stablerpg.stableeconomy.shop.exceptions.CannotBuyException;
import org.stablerpg.stableeconomy.shop.exceptions.NotEnoughSpaceException;
import org.stablerpg.stableeconomy.shop.exceptions.SellException;
import org.stablerpg.stableeconomy.shop.util.InventoryUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class TransactableItem implements Itemable {

  private final EconomyAPI api;
  private final ItemBuilder itemBuilder;

  private final int amount;
  private final Component displayName;
  private final List<Component> description;

  private final double buyPrice;
  private final double sellValue;

  public TransactableItem(EconomyAPI api, ItemBuilder itemBuilder, int amount, Component displayName, List<Component> description, double buyPrice, double sellValue) {
    this.api = api;
    this.itemBuilder = itemBuilder;
    this.amount = amount;
    this.displayName = displayName;
    this.description = Collections.unmodifiableList(description);
    this.buyPrice = buyPrice;
    this.sellValue = sellValue;
  }

  public TransactableItem(EconomyAPI api, ItemBuilder itemBuilder, int amount, String displayName, List<String> description, double buyPrice, double sellValue) {
    this.api = api;
    this.itemBuilder = itemBuilder;
    this.amount = amount;
    this.displayName = MiniMessage.miniMessage().deserialize("<italic:false>" + displayName);
    this.description = description.stream().map(line -> "<italic:false>" + displayName).map(MiniMessage.miniMessage()::deserialize).toList();
    this.buyPrice = buyPrice;
    this.sellValue = sellValue;
  }

  public void purchase(Player player) throws BuyException {
    ItemStack item = this.itemBuilder.build();
    item.setAmount(amount);

    double buyPrice = this.buyPrice * amount;

    if (!api.hasBalance(player, buyPrice))
      throw new CannotBuyException("Not enough money to buy item");

    int availableSpace = InventoryUtil.getFittableAmount(player, item, amount);

    if (availableSpace < amount)
      throw new NotEnoughSpaceException("Not enough space in inventory");

    api.subtractBalance(player, buyPrice);
    PlayerGiveResult result = player.give(List.of(item), false);

    if (!result.leftovers().isEmpty())
      throw new BuyException("Failed to accurately detect available space in inventory");
  }

  public void sell(Player player) throws SellException {
    ItemStack item = this.itemBuilder.build();
    item.setAmount(amount);

    double sellValue = this.sellValue * amount;

    if (!player.getInventory().containsAtLeast(item, amount))
      throw new SellException("You don't have the item to sell");

    api.addBalance(player, sellValue);
    player.getInventory().removeItem(item);
  }

  @Override
  public ItemStack build() {
    return this.itemBuilder.copy(builder -> {
      List<Component> description = this.description != null ? new ArrayList<>(this.description) : new ArrayList<>();
      description.add(Component.empty());
      description.add(MiniMessage.miniMessage().deserialize("<italic:false><gray>Price:</gray> <yellow>$" + buyPrice * amount + "</yellow>"));
      builder.displayName(displayName).lore(description).itemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DYE, ItemFlag.HIDE_UNBREAKABLE);
    }).build();
  }

}
