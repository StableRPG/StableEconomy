package org.stablerpg.stableeconomy.shop.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class InventoryUtil {

  public static boolean canFit(@NotNull Player player, @NotNull ItemStack item) {
    return canFit(player.getInventory(), item);
  }

  public static boolean canFit(@NotNull Inventory inventory, @NotNull ItemStack item) {
    if (item.getType().isAir())
      return true;

    int amount = 0;

    final int maxStackSize = item.getMaxStackSize();

    ItemStack[] items = inventory.getStorageContents();

    for (ItemStack itemStack : items) {
      if (itemStack == null || itemStack.getType().isAir())
        amount += maxStackSize;
      else if (itemStack.isSimilar(item)) {
        int stackSize = itemStack.getAmount();
        if (maxStackSize > stackSize)
          amount += maxStackSize - stackSize;
      }
      if (amount >= item.getAmount())
        return true;
    }

    return false;
  }

  public static int getFittableAmount(@NotNull Player player, @NotNull ItemStack item) {
    return getFittableAmount(player.getInventory(), item);
  }

  public static int getFittableAmount(@NotNull Inventory inventory, @NotNull ItemStack item) {
    if (item.getType().isAir())
      return -1;

    int amount = 0;

    final int maxStackSize = item.getMaxStackSize();

    ItemStack[] items = inventory.getStorageContents();

    for (ItemStack itemStack : items) {
      if (itemStack == null || itemStack.getType().isAir())
        amount += maxStackSize;
      else if (itemStack.isSimilar(item)) {
        int stackSize = itemStack.getAmount();
        if (maxStackSize > stackSize)
          amount += maxStackSize - stackSize;
      }
    }

    return amount;
  }

  private InventoryUtil () {
    throw new UnsupportedOperationException("This class cannot be instantiated");
  }

}