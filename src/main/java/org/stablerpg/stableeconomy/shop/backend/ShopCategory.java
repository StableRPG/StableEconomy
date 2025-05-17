package org.stablerpg.stableeconomy.shop.backend;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.stablerpg.stableeconomy.shop.ShopManager;
import org.stablerpg.stableeconomy.shop.frontend.ShopCategoryViewTemplate;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ShopCategory {

  private final ShopManager manager;

  private final String id;

  private final Component title;

  private final ShopCategoryViewTemplate context;
  private final Map<Integer, TransactableItem> transactableItems = new HashMap<>();
  private final Map<Integer, ShopItem> shopItems = new HashMap<>();

  public ShopCategory(ShopManager manager, String id, Component title, ShopCategoryViewTemplate context) {
    this.manager = manager;
    this.id = id;
    this.title = title;
    this.context = context;
  }

  public void addTransactableItem(Integer slot, TransactableItem item) {
    transactableItems.put(slot, item);
  }

  public void addShopItem(Integer slot, ShopItem item) {
    shopItems.put(slot, item);
  }

}
