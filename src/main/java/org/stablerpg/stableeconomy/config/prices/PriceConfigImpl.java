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
import java.util.Set;
import java.util.regex.Pattern;

public class PriceConfigImpl extends AbstractConfig implements PriceConfig, PriceProvider {

  private final PriceProviderImpl priceProvider;

  public PriceConfigImpl(@NotNull AbstractEconomyPlugin plugin) {
    super(plugin, "prices.yml", false);
    this.priceProvider = new PriceProviderImpl();
  }

  @Override
  public void load() {
    super.load();

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

  @Override
  public void close() {
    priceProvider.reset();
  }

  @SuppressWarnings("DataFlowIssue")
  private PricedItem deserialize(@NotNull ConfigurationSection section) {
    Set<String> keys = section.getKeys(false);

    if (keys.contains("buy-price") && keys.contains("sell-value")) {
      double buyPrice = section.getDouble("buy-price", -1);
      double sellValue = section.getDouble("sell-value", -1);

      if (buyPrice == -1) {
        getPlugin().getLogger().warning("No buy price specified for priced item: " + section.getName());
        return null;
      }

      if (sellValue == -1) {
        getPlugin().getLogger().warning("No value specified for priced item: " + section.getName());
        return null;
      }

      if (keys.contains("material")) {
        Pattern material = Pattern.compile(section.getString("material"));
        Pattern name = null;
        if (keys.contains("name")) name = Pattern.compile(section.getString("display-name"));
        return new AdvancedPricedItem(material, name, buyPrice, sellValue);
      }

      if (keys.contains("materials")) {
        List<String> rawMaterials = section.getStringList("materials");
        Material[] materials = new Material[rawMaterials.size()];

        int i = 0;
        for (String rawMaterial : rawMaterials) {
          Material material = Material.matchMaterial(rawMaterial);
          if (material == null) {
            getPlugin().getLogger().warning("Invalid material specified for %s: %s".formatted(section.getName(), rawMaterial));
            return null;
          }
          materials[i++] = material;
        }

        return new GroupedPricedItems(materials, buyPrice, sellValue);
      }

      String rawMaterial = section.getName().toUpperCase();
      Material material = Material.matchMaterial(rawMaterial);
      if (material == null) {
        getPlugin().getLogger().warning("Invalid material specified for %s: %s".formatted(section.getName(), rawMaterial));
        return null;
      }
      return new BasicPricedItem(material, buyPrice, sellValue);
    }

    getPlugin().getLogger().warning("Invalid priced item section: " + section.getName());

    return null;
  }

  public @NotNull PriceProvider getPriceProvider() {
    return this;
  }

  @Override
  public double getBuyPrice(ItemStack itemStack) {
    return priceProvider.getBuyPrice(itemStack);
  }

  @Override
  public double getSellValue(ItemStack itemStack) {
    return priceProvider.getSellValue(itemStack);
  }

}
