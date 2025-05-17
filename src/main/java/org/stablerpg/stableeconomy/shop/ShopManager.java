package org.stablerpg.stableeconomy.shop;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.shop.exceptions.CannotBuyException;
import org.stablerpg.stableeconomy.shop.exceptions.NotSellableException;
import org.stablerpg.stableeconomy.shop.exceptions.NothingSellableException;
import org.stablerpg.stableeconomy.shop.exceptions.SellAirException;
import org.stablerpg.stableeconomy.shop.frontend.ShopCategoryView;

public class ShopManager extends AbstractShopManager {

  public ShopManager(EconomyPlatform platform) {
    super(platform);
  }

  @Override
  public void load() {
    CommandTree shopCommand = new CommandTree("shop")
      .executesPlayer((player, args) -> {
        if (getMainCategory() == null) {
          throw CommandAPI.failWithString("Shop not found! Contact an administrator immediately.");
        }
        new ShopCategoryView(getMainCategory()).open(player);
      });
    shopCommand.register();
  }

  @Override
  public void close() {
    CommandAPI.unregister("shop");
    resetCategories();
  }

  @Override
  public void sellHand(Player player) throws NotSellableException, SellAirException {
    ItemStack item = player.getInventory().getItemInMainHand();

    if (item.getType().isAir())
      throw new SellAirException();

    double value = getPlatform().getPriceProvider().getSellValue(item);

    if (value == -1)
      throw new NotSellableException();

    player.getInventory().setItemInMainHand(null);
    getPlatform().addBalance(player, value);
  }

  @Override
  public void sellItem(Player player, ItemStack item) throws NotSellableException, SellAirException, NothingSellableException {
    if (item.getType().isAir())
      throw new SellAirException();

    if (getPlatform().getPriceProvider().getSellValue(item) == -1)
      throw new NotSellableException();

    double value = 0;

    for (ItemStack stack : player.getInventory().getContents()) {
      if (stack != null && stack.isSimilar(item)) {
        player.getInventory().removeItem(stack);
        value += getPlatform().getPriceProvider().getSellValue(stack);
      }
    }

    if (value == 0)
      throw new NothingSellableException();

    getPlatform().addBalance(player, value);
  }

  @Override
  public void sellInventory(Player player) throws NothingSellableException {
    double totalValue = 0;

    for (ItemStack item : player.getInventory().getContents()) {
      if (item != null) {
        totalValue += getPlatform().getPriceProvider().getSellValue(item);
        player.getInventory().removeItem(item);
      }
    }

    if (totalValue == 0)
      throw new NothingSellableException();

    getPlatform().addBalance(player, totalValue);
  }

  @Override
  public void buyItem(Player player, ItemStack item) throws CannotBuyException {
    double price = getPlatform().getPriceProvider().getBuyPrice(item);

    if (price == -1)
      throw new CannotBuyException();

    player.getInventory().addItem(item);
    getPlatform().subtractBalance(player, price);
  }

}
