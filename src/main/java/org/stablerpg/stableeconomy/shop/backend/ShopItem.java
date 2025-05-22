package org.stablerpg.stableeconomy.shop.backend;

import dev.triumphteam.gui.click.ClickContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;
import org.stablerpg.stableeconomy.shop.ShopManager;
import org.stablerpg.stableeconomy.shop.gui.AbstractGuiItem;
import org.stablerpg.stableeconomy.shop.gui.ShopCategoryView;

@RequiredArgsConstructor
@Getter
public class ShopItem implements AbstractGuiItem {

  public static ShopItem deserialize(ShopManager manager, ConfigurationSection section) throws DeserializationException {
    ItemBuilder itemBuilder = ItemBuilder.deserialize(section);

    String rawActionType = section.getString("action", "NONE").toUpperCase();
    ShopItemAction action = ShopItemAction.valueOf(rawActionType);
    String[] actionArgs;

    actionArgs = switch (action) {
      case OPEN_CATEGORY -> {
        String category = section.getString("category");
        if (category == null) {
          throw new DeserializationException("Failed to locate category for action OPEN_CATEGORY in " + section.getName());
        }
        yield new String[]{category};
      }
      default -> new String[0];
    };

    return new ShopItem(manager, itemBuilder, action, actionArgs);
  }

  private final ShopManager manager;

  private final ItemBuilder itemBuilder;

  private final @NotNull ShopItemAction action;
  private final @Nullable String[] actionArgs;

  public void execute(Player player, ClickContext context) {
    if (action == ShopItemAction.NONE)
      return;

    if (action == ShopItemAction.OPEN_CATEGORY) {
      if (actionArgs == null || actionArgs.length == 0) {
        manager.getPlatform().getLogger().warning("Shop category ID not specified");
        return;
      }

      ShopCategory category = manager.getCategory(actionArgs[0]);
      if (category == null) {
        manager.getPlatform().getLogger().warning("Shop category " + actionArgs[0] + " not found");
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
