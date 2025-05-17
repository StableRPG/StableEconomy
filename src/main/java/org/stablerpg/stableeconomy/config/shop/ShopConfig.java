package org.stablerpg.stableeconomy.config.shop;

import org.stablerpg.stableeconomy.config.BasicConfig;
import org.stablerpg.stableeconomy.shop.ShopManager;

import java.io.Closeable;

public interface ShopConfig extends BasicConfig, Closeable {

  ShopManager getShopManager();

}
