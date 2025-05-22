package org.stablerpg.stableeconomy.shop.gui;

import dev.triumphteam.gui.click.ClickContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface AbstractGuiItem {

  void execute(Player player, ClickContext context);

  ItemStack build(Player player);

}
