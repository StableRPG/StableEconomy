package org.stablerpg.stableeconomy.prices;

import org.bukkit.inventory.ItemStack;

import java.util.function.Predicate;

public interface PricedItem extends Predicate<ItemStack> {

  boolean test(ItemStack item);

  double getBuyPrice();

  double getSellPrice();

}
