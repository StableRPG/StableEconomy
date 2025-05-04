package org.stablerpg.stableeconomy.config.currency;

import org.stablerpg.stableeconomy.config.BasicConfig;
import org.stablerpg.stableeconomy.currency.Currency;

import java.util.Collection;
import java.util.Optional;

public interface CurrencyHolder extends BasicConfig {

  void registerCurrencies();

  void unregisterCurrencies();

  Currency getDefaultCurrency();

  Collection<Currency> getCurrencies();

  Optional<Currency> getCurrency(String name);

}
