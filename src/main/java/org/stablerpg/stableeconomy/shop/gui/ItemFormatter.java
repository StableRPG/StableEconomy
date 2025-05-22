package org.stablerpg.stableeconomy.shop.gui;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record ItemFormatter(@NotNull String nameFormat, @NotNull String loreFormat, @NotNull String buyPriceLore, @NotNull String sellValueLore,
                            boolean supportsPlaceholderAPI) {

  public static ItemFormatter deserialize(ConfigurationSection section, ItemFormatter def) {
    String nameFormat = section.getString("name-format", def.nameFormat())
      .replace("<name>", "%s");
    String loreFormat = section.getString("lore-format", def.loreFormat())
      .replace("<lore>", "%s");
    String buyPriceLore = section.getString("buy-price-lore", def.buyPriceLore())
      .replace("<amount>", "%s");
    String sellValueLore = section.getString("sell-value-lore", def.sellValueLore())
      .replaceAll("<amount>", "%s");

    return new ItemFormatter(nameFormat, loreFormat, buyPriceLore, sellValueLore, def.supportsPlaceholderAPI);
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

    boolean supportsPlaceholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

    return new ItemFormatter(nameFormat, loreFormat, buyPriceLore, sellValueLore, supportsPlaceholderAPI);
  }

  public List<String> format(List<String> strs, Function<String, String> formatter, Player player) {
    if (strs == null)
      return new ArrayList<>();

    List<String> formatted = new ArrayList<>(strs);

    for (int i = 0; i < formatted.size(); i++) {
      String str = formatted.get(i);
      if (str == null)
        continue;
      str = formatter.apply(str);
      formatted.set(i, str);
    }

    if (!supportsPlaceholderAPI)
      return formatted;

    for (int i = 0; i < formatted.size(); i++) {
      String str = formatted.get(i);
      if (str == null)
        continue;
      str = PlaceholderAPI.setPlaceholders(player, str);
      str = PlaceholderAPI.setBracketPlaceholders(player, str);
      formatted.set(i, str);
    }

    return formatted;
  }

  public String format(String str, Function<String, String> formatter, Player player) {
    if (str == null)
      return "";

    str = formatter.apply(str);

    if (!supportsPlaceholderAPI)
      return str;

    str = PlaceholderAPI.setPlaceholders(player, str);
    str = PlaceholderAPI.setBracketPlaceholders(player, str);

    return str;
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
