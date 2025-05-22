package org.stablerpg.stableeconomy.prices;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.api.PriceProvider;

import java.util.ArrayList;
import java.util.List;

public class PriceProviderImpl implements PriceProvider {

  private final List<PricedItem> pricedItems;

  public PriceProviderImpl() {
    this.pricedItems = new ArrayList<>();
  }

  public void add(@NotNull PricedItem pricedItem) {
    pricedItems.add(pricedItem);
  }

  public void reset() {
    pricedItems.clear();
  }

  @Override
  public double getBuyPrice(ItemStack itemStack) {
    PricedItem pricedItem = getPricedItem(itemStack);
    if (pricedItem != null) return pricedItem.getBuyPrice() * itemStack.getAmount();
    return -1;
  }

  @Override
  public double getSellValue(ItemStack itemStack) {
    PricedItem pricedItem = getPricedItem(itemStack);
    if (pricedItem != null) return pricedItem.getSellValue() * itemStack.getAmount();
    return -1;
  }

  private PricedItem getPricedItem(ItemStack itemStack) {
    for (PricedItem item : pricedItems)
      if (item.test(itemStack))
        return item;
    return null;
  }

}
