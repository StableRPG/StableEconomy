package org.stablerpg.stableeconomy.prices;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BasicPricedItem extends PricedItem {

  private final @NotNull Material material;

  public BasicPricedItem(@NotNull Material material, double buyPrice, double sellPrice) {
    super(buyPrice, sellPrice);
    this.material = material;
  }

  @Override
  public boolean test(@NotNull ItemStack item) {
    return item.getType() == material;
  }

}
