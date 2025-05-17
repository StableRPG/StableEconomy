package org.stablerpg.stableeconomy.shop.backend;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

@NoArgsConstructor
@AllArgsConstructor
public final class ItemBuilder implements Itemable {

  public static ItemBuilder of(ConfigurationSection section) {
    ItemBuilder builder = new ItemBuilder();

    String materialName = section.getString("material");
    if (materialName != null) {
      Material material = Material.matchMaterial(materialName);
      if (material == null)
        return null;
      builder.material(material);
    }

    builder.amount(section.getInt("amount", 1));

    String displayName = section.getString("display-name");
    if (displayName != null)
      builder.displayName(displayName);

    List<String> lore = section.getStringList("lore");
    if (!lore.isEmpty())
      builder.lore(lore.toArray(new String[0]));

    List<String> flags = section.getStringList("flags");
    if (!flags.isEmpty()) {
      ItemFlag[] itemFlags = flags.stream()
        .map(ItemFlag::valueOf)
        .toArray(ItemFlag[]::new);
      builder.itemFlags(itemFlags);
    }

    return builder;
  }

  private Material material;
  private int amount = 1;
  private Component displayName;
  private List<Component> lore;
  private ItemFlag[] itemFlags;

  private ItemBuilder(ItemBuilder builder) {
    this.material = builder.material;
    this.amount = builder.amount;
    this.displayName = builder.displayName;
    this.lore = builder.lore;
    this.itemFlags = builder.itemFlags;
  }

  public Material material() {
    return material;
  }

  public ItemBuilder material(Material material) {
    this.material = material;
    return this;
  }

  public int amount() {
    return amount;
  }

  public ItemBuilder amount(int amount) {
    this.amount = amount;
    return this;
  }

  public Component displayName() {
    return displayName;
  }

  public ItemBuilder displayName(String displayName) {
    return displayName(MiniMessage.miniMessage().deserialize("<italic:false>" + displayName));
  }

  public ItemBuilder displayName(Component displayName) {
    this.displayName = displayName;
    return this;
  }

  public List<Component> lore() {
    return lore;
  }

  public ItemBuilder lore(List<Component> lore) {
    this.lore = lore;
    return this;
  }

  public ItemBuilder lore(String... lore) {
    this.lore = Stream.of(lore)
      .map(MiniMessage.miniMessage()::deserialize)
      .toList();
    return this;
  }

  public ItemBuilder lore(Component... lore) {
    this.lore = List.of(lore);
    return this;
  }

  public ItemFlag[] itemFlags() {
    return itemFlags;
  }

  public ItemBuilder itemFlags(ItemFlag... itemFlags) {
    this.itemFlags = itemFlags;
    return this;
  }

  public ItemBuilder copy(Consumer<ItemBuilder> consumer) {
    ItemBuilder copy = new ItemBuilder(this);
    consumer.accept(copy);
    return copy;
  }

  @Override
  public ItemStack build() {
    ItemStack item = ItemStack.of(material, amount);
    item.editMeta(meta -> {
      meta.displayName(displayName);
      if (lore != null)
        meta.lore(lore);
      if (itemFlags != null && itemFlags.length > 0)
        meta.addItemFlags(itemFlags);
    });

    return item;
  }

}
