package org.stablerpg.stableeconomy.api;

import org.bukkit.inventory.ItemStack;

public interface PriceProvider {

  double getSellPrice(ItemStack itemStack);

  double getBuyPrice(ItemStack itemStack);

}
