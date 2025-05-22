package org.stablerpg.stableeconomy.shop.gui;

import dev.triumphteam.gui.paper.Gui;
import dev.triumphteam.gui.paper.builder.item.ItemBuilder;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.shop.backend.ShopCategory;

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
        dev.triumphteam.gui.element.GuiItem<Player, ItemStack> background = ItemBuilder.from(backgroundItem).asGuiItem();
        for (int slot : category.getContext().backgroundSlots())
          container.setItem(slot, background);

        for (Map.Entry<Integer, AbstractGuiItem> entry : category.getItems().entrySet()) {
          final AbstractGuiItem item = entry.getValue();
          container.setItem(entry.getKey(), ItemBuilder.from(item.build()).asGuiItem(item::execute));
        }
      }).build();

    gui.open(player);
  }

}
