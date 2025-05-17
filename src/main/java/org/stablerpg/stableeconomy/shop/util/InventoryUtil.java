package org.stablerpg.stableeconomy.shop.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InventoryUtil {

  public static int getFittableAmount(@NotNull Player player, @NotNull Material material, int amount) {
    if (amount <= 0) {
      return -1;
    }
    if (material.isAir()) {
      return -1;
    }
    ItemStack itemTypeStack = ItemStack.of(material);
    return doCalculateFittableAmount(player.getInventory(), itemTypeStack, amount);
  }

  public static int getFittableAmount(@NotNull Player player, @NotNull ItemStack item, int amount) {
    if (amount <= 0 || item.getType().isAir())
      return -1;

    return doCalculateFittableAmount(player.getInventory(), item, amount);
  }

  public static int getFittableAmount(@NotNull Inventory inventory, @NotNull ItemStack item, int amount) {
    if (amount <= 0 || item.getType().isAir())
      return -1;

    return doCalculateFittableAmount(inventory, item, amount);
  }

  private static int doCalculateFittableAmount(@NotNull Inventory inventory, @NotNull ItemStack item, int amount) {
    int amountFitted = 0;
    int quantityRemainingToFit = amount;

    final int maxStackSize = item.getMaxStackSize();
    if (maxStackSize <= 0)
      return 0;

    ItemStack[] storageContents = inventory instanceof PlayerInventory ? inventory.getStorageContents() : inventory.getContents();

    for (ItemStack currentSlotItem : storageContents) {
      if (quantityRemainingToFit <= 0)
        break;

      if (currentSlotItem != null && currentSlotItem.isSimilar(item)) {
        int existingAmount = currentSlotItem.getAmount();
        if (existingAmount < maxStackSize) {
          int spaceInStack = maxStackSize - existingAmount;
          int canPlaceThisIteration = Math.min(quantityRemainingToFit, spaceInStack);
          amountFitted += canPlaceThisIteration;
          quantityRemainingToFit -= canPlaceThisIteration;
        }
      }
    }

    if (quantityRemainingToFit > 0) {
      for (ItemStack currentSlotItem : storageContents) {
        if (quantityRemainingToFit <= 0) {
          break;
        }
        if (currentSlotItem == null || currentSlotItem.getType().isAir()) {
          int canPlaceThisIteration = Math.min(quantityRemainingToFit, maxStackSize);
          amountFitted += canPlaceThisIteration;
          quantityRemainingToFit -= canPlaceThisIteration;
        }
      }
    }

    return amountFitted;
  }
}