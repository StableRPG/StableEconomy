package org.stablerpg.stableeconomy.shop;

import lombok.Getter;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.config.shop.ShopLocale;
import org.stablerpg.stableeconomy.shop.backend.ShopCategory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ShopManager {

  @Getter
  private final EconomyPlatform platform;

  private final Collection<ShopCommand> commands = new HashSet<>();

  private final Map<String, ShopLocale> locales = new HashMap<>();

  private final Map<String, ShopCategory> categories = new HashMap<>();

  public ShopManager(EconomyPlatform platform) {
    this.platform = platform;
  }

  public void load() {
    for (ShopCommand command : commands)
      command.register();
  }

  public void close() {
    resetCategories();
    resetLocales();
    resetCommands();
  }

  public Collection<ShopCommand> getCommands() {
    return Collections.unmodifiableCollection(commands);
  }

  public void addCommand(ShopCommand command) {
    commands.add(command);
  }

  private void resetCommands() {
    for (ShopCommand command : commands)
      command.unregister();
    commands.clear();
  }

  public Collection<ShopLocale> getLocales() {
    return Collections.unmodifiableCollection(locales.values());
  }

  public ShopLocale getLocale(String id) {
    return locales.get(id);
  }

  public void addLocale(String id, ShopLocale locale) {
    locales.put(id, locale);
  }

  private void resetLocales() {
    locales.clear();
  }

  public Collection<ShopCategory> getCategories() {
    return Collections.unmodifiableCollection(categories.values());
  }

  public ShopCategory getCategory(String id) {
    return categories.get(id);
  }

  public void addCategory(String id, ShopCategory category) {
    categories.put(id, category);
  }

  private void resetCategories() {
    categories.clear();
  }

}
