package org.stablerpg.stableeconomy.prices;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class AdvancedPricedItem extends PricedItem {

  private final @NotNull Pattern material;
  private final @Nullable Pattern name;

  public AdvancedPricedItem(@NotNull Pattern material, @Nullable Pattern name, double buyPrice, double sellValue) {
    super(buyPrice, sellValue);
    this.material = material;
    this.name = name;
  }

  @Override
  public boolean test(@NotNull ItemStack item) {
    if (!material.matcher(item.getType().name()).matches()) return false;
    if (name == null) return true;
    Component rawName = item.displayName();
    String itemName = PlainTextComponentSerializer.plainText().serialize(rawName);
    return name.matcher(itemName).matches();
  }

}
