package org.stablerpg.stableeconomy.prices;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;

import java.util.function.Predicate;

@Getter
public abstract class PricedItem implements Predicate<ItemStack> {

  public static PricedItem deserialize(ConfigurationSection section) throws DeserializationException {
    if (section.contains("buy-price") && section.contains("sell-value")) {
      double buyPrice = section.getDouble("buy-price", -1);
      double sellValue = section.getDouble("sell-value", -1);

      if (buyPrice == -1 && sellValue == -1)
        throw new DeserializationException("Failed to find buy-price or sell-value");

      if (section.contains("material"))
        return AdvancedPricedItem.deserialize(section, buyPrice, sellValue);

      if (section.contains("materials"))
        return GroupedPricedItem.deserialize(section, buyPrice, sellValue);

      return BasicPricedItem.deserialize(section, buyPrice, sellValue);
    }

    throw new DeserializationException("Failed to identify PricedItem type");
  }

  private final double buyPrice;
  private final double sellValue;

  public PricedItem(double buyPrice, double sellValue) {
    this.buyPrice = buyPrice;
    this.sellValue = sellValue;
  }

}
