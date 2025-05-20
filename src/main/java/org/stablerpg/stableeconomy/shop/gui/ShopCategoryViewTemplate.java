package org.stablerpg.stableeconomy.shop.gui;

import org.bukkit.configuration.ConfigurationSection;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;
import org.stablerpg.stableeconomy.shop.backend.ItemBuilder;

public record ShopCategoryViewTemplate(int rows, ItemBuilder background, int[] backgroundSlots) {

 public static ShopCategoryViewTemplate deserialize(ConfigurationSection section) throws DeserializationException {
   int rows = section.getInt("rows", 3);
   ConfigurationSection backgroundItemSection = section.getConfigurationSection("background-item");
   if (backgroundItemSection == null)
     throw new DeserializationException("Failed to locate background item section");
   ItemBuilder backgroundItem = ItemBuilder.deserialize(backgroundItemSection);
   int[] backgroundSlots = section.getIntegerList("background-slots").stream().mapToInt(Integer::intValue).toArray();
   return new ShopCategoryViewTemplate(rows, backgroundItem, backgroundSlots);
 }

}
