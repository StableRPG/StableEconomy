package org.stablerpg.stableeconomy.shop.gui;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public record ItemFormatter(@NotNull String nameFormat, @NotNull String loreFormat, @NotNull String buyPriceLore, @NotNull String sellValueLore) {

  public static ItemFormatter deserialize(ConfigurationSection section, ItemFormatter def) {
    String nameFormat = section.getString("name-format", def.nameFormat())
      .replace("<name>", "%s");
    String loreFormat = section.getString("lore-format", def.loreFormat())
      .replace("<lore>", "%s");
    String buyPriceLore = section.getString("buy-price-lore", def.buyPriceLore())
      .replace("<amount>", "%s");
    String sellValueLore = section.getString("sell-value-lore", def.sellValueLore())
      .replaceAll("<amount>", "%s");

    return new ItemFormatter(nameFormat, loreFormat, buyPriceLore, sellValueLore);
  }

  public static ItemFormatter deserialize(ConfigurationSection section) {
    String nameFormat = section.getString("name-format", "<italic:false><gray><name>")
      .replace("<name>", "%s");
    String loreFormat = section.getString("lore-format", "<italic:false><gray><lore>")
      .replace("<lore>", "%s");
    String buyPriceLore = section.getString("buy-price-lore", "<italic:false><gray>Price:</gray> <yellow><amount></yellow>")
      .replace("<amount>", "%s");
    String sellValueLore = section.getString("sell-value-lore", "<italic:false><gray>Sell Value:</gray> <yellow><amount></yellow>")
      .replaceAll("<amount>", "%s");

    return new ItemFormatter(nameFormat, loreFormat, buyPriceLore, sellValueLore);
  }

  public String formatName(String name) {
    return String.format(nameFormat, name);
  }

  public String formatLore(String lore) {
    return String.format(loreFormat, lore);
  }

  public String formatBuyPriceLore(String price) {
    return String.format(buyPriceLore, price);
  }

  public String formatSellValueLore(String value) {
    return String.format(sellValueLore, value);
  }

}
