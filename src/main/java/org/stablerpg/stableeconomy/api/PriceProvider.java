package org.stablerpg.stableeconomy.api;

import org.bukkit.inventory.ItemStack;

public interface PriceProvider {

  double getBuyPrice(ItemStack itemStack);

  double getSellValue(ItemStack itemStack);

}
