package org.stablerpg.stableeconomy.shop.frontend;

import dev.triumphteam.gui.click.GuiClick;
import dev.triumphteam.gui.element.GuiItem;
import dev.triumphteam.gui.paper.Gui;
import dev.triumphteam.gui.paper.builder.item.ItemBuilder;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.shop.backend.ShopCategory;
import org.stablerpg.stableeconomy.shop.backend.ShopItem;
import org.stablerpg.stableeconomy.shop.backend.TransactableItem;
import org.stablerpg.stableeconomy.shop.exceptions.BuyException;
import org.stablerpg.stableeconomy.shop.exceptions.CannotBuyException;
import org.stablerpg.stableeconomy.shop.exceptions.NotEnoughSpaceException;

import java.util.Map;

@RequiredArgsConstructor
public class ShopCategoryView {

  private final @NotNull ShopCategory category;

  public void open(Player player) {
    Gui gui = Gui.of(category.getContext().rows())
      .title(category.getTitle())
      .statelessComponent(container -> {
        ItemStack backgroundItem = category.getContext().background().build();
        backgroundItem.editMeta(meta -> meta.displayName(Component.space()));
        GuiItem<Player, ItemStack> background = ItemBuilder.from(backgroundItem).asGuiItem();
        for (int slot : category.getContext().backgroundSlots())
          container.setItem(slot, background);

        for (Map.Entry<Integer, TransactableItem> entry : category.getTransactableItems().entrySet()) {
          final TransactableItem transactableItem = entry.getValue();

          GuiItem<Player, ItemStack> guiItem = ItemBuilder.from(transactableItem.build()).asGuiItem((clicker, context) -> {
            if (context.guiClick().equals(GuiClick.LEFT) || context.guiClick().equals(GuiClick.SHIFT_LEFT)) {
              try {
                transactableItem.purchase(clicker);
              } catch (CannotBuyException e) {
                clicker.sendRichMessage("<red>Not enough money to buy item!</red>");
              } catch (NotEnoughSpaceException e) {
                clicker.sendRichMessage("<red>Not enough space in inventory!</red>");
              } catch (BuyException e) {
                clicker.sendRichMessage("<red>Failed to accurately detect available space in inventory!</red>");
              }
            } else if (context.guiClick().equals(GuiClick.RIGHT) || context.guiClick().equals(GuiClick.SHIFT_RIGHT)) {
              try {
                transactableItem.sell(clicker);
              } catch (NotEnoughSpaceException e) {
                clicker.sendRichMessage("<red>You don't have the item to sell!</red>");
              }
            }
          });
          container.setItem(entry.getKey(), guiItem);
        }

        for (Map.Entry<Integer, ShopItem> entry : category.getShopItems().entrySet()) {
          final ShopItem item = entry.getValue();

          GuiItem<Player, ItemStack> guiItem = ItemBuilder.from(item.build()).asGuiItem((clicker, context) -> item.execute(clicker));

          container.setItem(entry.getKey(), guiItem);
        }
      }).build();

    gui.open(player);
  }

}
