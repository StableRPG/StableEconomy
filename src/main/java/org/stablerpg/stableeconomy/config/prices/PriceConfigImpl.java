package org.stablerpg.stableeconomy.config.prices;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.AbstractEconomyPlugin;
import org.stablerpg.stableeconomy.api.PriceProvider;
import org.stablerpg.stableeconomy.config.AbstractConfig;
import org.stablerpg.stableeconomy.prices.AdvancedPricedItem;
import org.stablerpg.stableeconomy.prices.BasicPricedItem;
import org.stablerpg.stableeconomy.prices.GroupedPricedItems;
import org.stablerpg.stableeconomy.prices.PriceProviderImpl;
import org.stablerpg.stableeconomy.prices.PricedItem;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class PriceConfigImpl extends AbstractConfig implements PriceConfig, PriceProvider {

  private final PriceProviderImpl priceProvider;

  public PriceConfigImpl(@NotNull AbstractEconomyPlugin plugin) {
    super(plugin, "prices.yml");
    super.automaticUpdate = false;
    this.priceProvider = new PriceProviderImpl();
  }

  @Override
  public void load() {
    load0();

    priceProvider.reset();

    ConfigurationSection section = getConfig().getConfigurationSection("prices");
    if (section == null) {
      getPlugin().getLogger().warning("No prices section found in prices.yml");
      return;
    }

    Set<String> items = section.getKeys(false);

    if (items.isEmpty()) {
      getPlugin().getLogger().warning("No priced items found in prices.yml");
      return;
    }

    for (String itemKey : items) {
      ConfigurationSection itemSection = section.getConfigurationSection(itemKey);
      if (itemSection == null) {
        getPlugin().getLogger().warning("Invalid priced item section: " + itemKey);
        continue;
      }

      PricedItem pricedItem = deserialize(itemSection);

      if (pricedItem == null) {
        getPlugin().getLogger().warning("Failed to deserialize priced item: " + itemKey);
        continue;
      }

      priceProvider.add(pricedItem);
    }
  }

  @SuppressWarnings("DataFlowIssue")
  private PricedItem deserialize(@NotNull ConfigurationSection section) {
    Set<String> keys = section.getKeys(false);
    if (keys.contains("buy") && keys.contains("sell")) {
      double buyPrice = section.getDouble("buy");
      double sellPrice = section.getDouble("sell");

      if (keys.contains("material")) {
        Pattern name = null;
        if (keys.contains("name")) name = Pattern.compile(section.getString("name"));
        Pattern material = Pattern.compile(section.getString("material"));
        return new AdvancedPricedItem(name, material, buyPrice, sellPrice);
      }

      if (keys.contains("materials")) {
        List<String> rawMaterials = section.getStringList("materials");
        Material[] materials = rawMaterials.stream().map(Material::matchMaterial).filter(Objects::nonNull).toArray(Material[]::new);
        return new GroupedPricedItems(materials, buyPrice, sellPrice);
      }

      String rawMaterial = section.getName().toUpperCase();
      Material material = Material.matchMaterial(rawMaterial);
      if (material == null) {
        getPlugin().getLogger().warning("Invalid material specified: " + rawMaterial);
        return null;
      }
      return new BasicPricedItem(material, buyPrice, sellPrice);
    }
    return null;
  }

  public @NotNull PriceProvider getPriceProvider() {
    return this;
  }

  @Override
  public double getSellPrice(ItemStack itemStack) {
    return priceProvider.getSellPrice(itemStack);
  }

  @Override
  public double getBuyPrice(ItemStack itemStack) {
    return priceProvider.getBuyPrice(itemStack);
  }

}
