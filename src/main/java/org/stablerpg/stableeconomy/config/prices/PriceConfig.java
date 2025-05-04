package org.stablerpg.stableeconomy.config.prices;

import org.stablerpg.stableeconomy.api.PriceProvider;
import org.stablerpg.stableeconomy.config.BasicConfig;

public interface PriceConfig extends BasicConfig {

  PriceProvider getPriceProvider();

}
