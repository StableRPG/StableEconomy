package org.stablerpg.stableeconomy.shop.backend;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stablerpg.stableeconomy.shop.ShopManager;
import org.stablerpg.stableeconomy.shop.frontend.ShopCategoryView;

@RequiredArgsConstructor
@Getter
public class ShopItem implements Itemable {

  private final ShopManager manager;

  private final ItemBuilder itemBuilder;

  private final @NotNull ShopItemAction action;
  private final @Nullable String[] actionArgs;

  public void execute(Player player) {
    if (action == ShopItemAction.NONE)
      return;

    if (action == ShopItemAction.OPEN_CATEGORY) {
      if (actionArgs == null || actionArgs.length == 0) {
        manager.getPlatform().getLogger().info("Shop category ID not specified");
        return;
      }

      ShopCategory category = manager.getCategory(actionArgs[0]);
      if (category == null) {
        manager.getPlatform().getLogger().info("Shop category " + actionArgs[0] + " not found");
        return;
      }

      new ShopCategoryView(category).open(player);
    }

    if (action == ShopItemAction.CLOSE_INVENTORY)
      player.closeInventory();
  }

  @Override
  public ItemStack build() {
    return itemBuilder.build();
  }
}
