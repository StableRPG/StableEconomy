package org.stablerpg.stableeconomy.prices;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

@AllArgsConstructor
public class AdvancedPricedItem implements PricedItem {

  private final @NotNull Pattern name;
  private final @NotNull Pattern material;

  @Getter
  private final double buyPrice;
  @Getter
  private final double sellPrice;

  @Override
  public boolean test(@NotNull ItemStack item) {
    if (!material.matcher(item.getType().name()).matches())
      return false;
    Component rawName = item.displayName();
    String itemName = PlainTextComponentSerializer.plainText().serialize(rawName);
    if (!name.matcher(itemName).matches())
      return false;
    return true;
  }

}
