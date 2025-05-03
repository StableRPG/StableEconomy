package org.stablerpg.stableeconomy.config.prices;

import org.stablerpg.stableeconomy.api.PriceProvider;

public interface PriceConfig {

  void load();

  PriceProvider getPriceProvider();

}
