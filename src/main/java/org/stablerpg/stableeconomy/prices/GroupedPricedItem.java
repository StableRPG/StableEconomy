package org.stablerpg.stableeconomy.prices;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;

import java.util.List;

public class GroupedPricedItem extends PricedItem {

  public static GroupedPricedItem deserialize(ConfigurationSection section, double buyPrice, double sellValue) throws DeserializationException {
    List<String> rawMaterials = section.getStringList("materials");
    Material[] materials = new Material[rawMaterials.size()];

    int i = 0;
    for (String rawMaterial : rawMaterials) {
      Material material = Material.matchMaterial(rawMaterial);
      if (material == null)
        throw new DeserializationException("Invalid material found \"%s\"".formatted(rawMaterial));
      materials[i++] = material;
    }

    return new GroupedPricedItem(materials, buyPrice, sellValue);

  }

  private final @NotNull Material[] materials;

  public GroupedPricedItem(@NotNull Material[] materials, double buyPrice, double sellValue) {
    super(buyPrice, sellValue);
    this.materials = materials;
  }

  @Override
  public boolean test(@NotNull ItemStack item) {
    Material itemType = item.getType();
    for (Material material : materials)
      if (material.equals(itemType)) return true;
    return false;
  }

}
