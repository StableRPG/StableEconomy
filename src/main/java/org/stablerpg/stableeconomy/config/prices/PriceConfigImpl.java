package org.stablerpg.stableeconomy.config.prices;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.AbstractEconomyPlugin;
import org.stablerpg.stableeconomy.api.PriceProvider;
import org.stablerpg.stableeconomy.config.AbstractConfig;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;
import org.stablerpg.stableeconomy.prices.PriceProviderImpl;
import org.stablerpg.stableeconomy.prices.PricedItem;

import java.util.Set;

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
      getLogger().warning("No prices section found in prices.yml");
      return;
    }

    Set<String> items = section.getKeys(false);

    if (items.isEmpty()) {
      getLogger().warning("No priced items found in prices.yml");
      return;
    }

    for (String itemKey : items) {
      ConfigurationSection itemSection = section.getConfigurationSection(itemKey);

      PricedItem pricedItem;
      try {
        //noinspection DataFlowIssue
        pricedItem = PricedItem.deserialize(itemSection);
      } catch (DeserializationException e) {
        getLogger().warning("Failed to deserialize priced item \"%s\": %s".formatted(itemSection.getName(), e.getMessage()));
        continue;
      }

      priceProvider.add(pricedItem);
    }
  }

  @Override
  public void close() {
    priceProvider.reset();
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
