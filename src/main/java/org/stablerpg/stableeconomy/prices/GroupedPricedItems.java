package org.stablerpg.stableeconomy.prices;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

@AllArgsConstructor
public class GroupedPricedItems implements PricedItem {

  private final @NotNull Pattern material;

  @Getter
  private final double buyPrice;
  @Getter
  private final double sellPrice;

  @Override
  public boolean test(@NotNull ItemStack item) {
    return material.matcher(item.getType().name()).matches();
  }

}
