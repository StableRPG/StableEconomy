package me.jeremiah.economy.config.currency;

import me.jeremiah.economy.currency.Currency;

import java.util.Set;

public interface CurrencyHolder {

  Currency getDefaultCurrency();
  Set<Currency> getCurrencies();

  void load();

}
