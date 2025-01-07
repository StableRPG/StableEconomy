package me.jeremiah.economy.config.currency;

import me.jeremiah.economy.currency.Currency;

import java.util.Collection;
import java.util.Optional;

public interface CurrencyHolder {

  Currency getDefaultCurrency();
  Collection<Currency> getCurrencies();
  Optional<Currency> getCurrency(String name);

  void load();

}
