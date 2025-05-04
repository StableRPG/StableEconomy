package org.stablerpg.stableeconomy.prices;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class GroupedPricedItems implements PricedItem {

  private final @NotNull Material[] materials;

  @Getter
  private final double buyPrice;
  @Getter
  private final double sellPrice;

  @Override
  public boolean test(@NotNull ItemStack item) {
    Material itemType = item.getType();
    for (Material material : materials)
      if (material.equals(itemType)) return true;
    return false;
  }

}
