package org.stablerpg.stableeconomy.shop;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.shop.backend.ShopCategory;
import org.stablerpg.stableeconomy.shop.exceptions.CannotBuyException;
import org.stablerpg.stableeconomy.shop.exceptions.NotSellableException;
import org.stablerpg.stableeconomy.shop.exceptions.NothingSellableException;
import org.stablerpg.stableeconomy.shop.exceptions.SellAirException;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractShopManager implements Closeable {

  @Getter
  private final EconomyPlatform platform;
  @Getter
  private ShopCategory mainCategory;
  private final Map<String, ShopCategory> categories = new HashMap<>();

  public abstract void load();

  public abstract void close();

  public void setMainCategory(ShopCategory mainCategory) {
    if (this.mainCategory != null)
      throw new IllegalStateException("Main category already set!");
    this.mainCategory = mainCategory;
  }

  public ShopCategory getCategory(String id) {
    return categories.get(id);
  }

  public void addCategory(ShopCategory category) {
    categories.put(category.getId(), category);
  }

  protected void resetCategories() {
    mainCategory = null;
    categories.clear();
  }

  public abstract void sellHand(Player player) throws NotSellableException, SellAirException;

  public abstract void sellItem(Player player, ItemStack item) throws NotSellableException, SellAirException, NothingSellableException;

  public abstract void sellInventory(Player player) throws NothingSellableException;

  public abstract void buyItem(Player player, ItemStack item) throws CannotBuyException;

}
