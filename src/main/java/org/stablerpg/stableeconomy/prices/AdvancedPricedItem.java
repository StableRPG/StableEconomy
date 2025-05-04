package org.stablerpg.stableeconomy.prices;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

@AllArgsConstructor
public class AdvancedPricedItem implements PricedItem {

  private final @Nullable Pattern name;
  private final @NotNull Pattern material;

  @Getter
  private final double buyPrice;
  @Getter
  private final double sellPrice;

  @Override
  public boolean test(@NotNull ItemStack item) {
    if (!material.matcher(item.getType().name()).matches()) return false;
    if (name == null) return true;
    Component rawName = item.displayName();
    String itemName = PlainTextComponentSerializer.plainText().serialize(rawName);
    return name.matcher(itemName).matches();
  }

}
