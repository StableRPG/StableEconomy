package org.stablerpg.stableeconomy.prices;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.function.Predicate;

@Getter
public abstract class PricedItem implements Predicate<ItemStack> {

  private final double buyPrice;
  private final double sellValue;

  public PricedItem(double buyPrice, double sellValue) {
    this.buyPrice = buyPrice;
    this.sellValue = sellValue;
  }

}
