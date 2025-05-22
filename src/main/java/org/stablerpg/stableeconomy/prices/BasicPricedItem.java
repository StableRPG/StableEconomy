package org.stablerpg.stableeconomy.prices;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;

public class BasicPricedItem extends PricedItem {

  public static BasicPricedItem deserialize(ConfigurationSection section, double buyPrice, double sellValue) throws DeserializationException {
    String rawMaterial = section.getName().toUpperCase();
    Material material = Material.matchMaterial(rawMaterial);
    if (material == null)
      throw new DeserializationException("Invalid material found \"%s\"".formatted(rawMaterial));
    return new BasicPricedItem(material, buyPrice, sellValue);
  }

  private final @NotNull Material material;

  public BasicPricedItem(@NotNull Material material, double buyPrice, double sellPrice) {
    super(buyPrice, sellPrice);
    this.material = material;
  }

  @Override
  public boolean test(@NotNull ItemStack item) {
    return item.getType() == material;
  }

}
