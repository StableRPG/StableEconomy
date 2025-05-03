package org.stablerpg.stableeconomy.prices;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.api.PriceProvider;

import java.util.HashSet;
import java.util.Set;

public class PriceProviderImpl implements PriceProvider {

  private final Set<BasicPricedItem> basic;
  private final Set<GroupedPricedItems> grouped;
  private final Set<AdvancedPricedItem> advanced;

  public PriceProviderImpl() {
    this.basic = new HashSet<>();
    this.grouped = new HashSet<>();
    this.advanced = new HashSet<>();
  }

  public void add(@NotNull PricedItem pricedItem) {
    switch (pricedItem) {
      case BasicPricedItem basicPricedItem -> basic.add(basicPricedItem);
      case GroupedPricedItems groupedPricedItems -> grouped.add(groupedPricedItems);
      case AdvancedPricedItem advancedPricedItem -> advanced.add(advancedPricedItem);
      default -> throw new IllegalArgumentException("Unknown PricedItem type: " + pricedItem.getClass().getName());
    }
  }

  public void reset() {
    basic.clear();
    grouped.clear();
    advanced.clear();
  }

  private PricedItem getPricedItem(ItemStack itemStack) {
    for (BasicPricedItem item : basic)
      if (item.test(itemStack))
        return item;
    for (GroupedPricedItems item : grouped)
      if (item.test(itemStack))
        return item;
    for (AdvancedPricedItem item : advanced)
      if (item.test(itemStack))
        return item;
    return null;
  }

  @Override
  public double getSellPrice(ItemStack itemStack) {
    PricedItem pricedItem = getPricedItem(itemStack);
    if (pricedItem != null)
      return pricedItem.getSellPrice() * itemStack.getAmount();
    return 0;
  }

  @Override
  public double getBuyPrice(ItemStack itemStack) {
    PricedItem pricedItem = getPricedItem(itemStack);
    if (pricedItem != null)
      return pricedItem.getBuyPrice() * itemStack.getAmount();
    return 0;
  }

}
