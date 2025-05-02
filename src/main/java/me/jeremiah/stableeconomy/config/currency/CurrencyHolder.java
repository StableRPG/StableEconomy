package me.jeremiah.stableeconomy.config.currency;

import me.jeremiah.stableeconomy.currency.Currency;

import java.util.Collection;
import java.util.Optional;

public interface CurrencyHolder {

  Currency getDefaultCurrency();
  Collection<Currency> getCurrencies();
  Optional<Currency> getCurrency(String name);

  void load();

}
