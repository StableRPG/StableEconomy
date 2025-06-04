package org.stablerpg.stableeconomy.prices;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;

import java.util.regex.Pattern;

public class AdvancedPricedItem extends PricedItem {

  public static AdvancedPricedItem deserialize(ConfigurationSection section, double buyPrice, double sellValue) throws DeserializationException {
    String rawMaterial = section.getString("material");

    if (rawMaterial == null || rawMaterial.isEmpty())
      throw new DeserializationException("Missing or empty 'material' field in section: " + section.getName());

    Pattern material = Pattern.compile(rawMaterial);

    String rawName = section.getString("display-name");
    Pattern name = null;

    if (rawName != null && !rawName.isEmpty())
      name = Pattern.compile(rawName);

    return new AdvancedPricedItem(material, name, buyPrice, sellValue);
  }

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
