package org.stablerpg.stableeconomy.prices;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class BasicPricedItem implements PricedItem {

  private final @NotNull Material material;

  @Getter
  private final double buyPrice;
  @Getter
  private final double sellPrice;

  @Override
  public boolean test(@NotNull ItemStack item) {
    return item.getType() == material;
  }

}
