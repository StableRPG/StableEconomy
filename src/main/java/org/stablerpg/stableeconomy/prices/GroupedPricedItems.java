package org.stablerpg.stableeconomy.prices;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GroupedPricedItems extends PricedItem {

  private final @NotNull Material[] materials;

  public GroupedPricedItems(@NotNull Material[] materials, double buyPrice, double sellValue) {
    super(buyPrice, sellValue);
    this.materials = materials;
  }

  @Override
  public boolean test(@NotNull ItemStack item) {
    Material itemType = item.getType();
    for (Material material : materials)
      if (material.equals(itemType)) return true;
    return false;
  }

}
