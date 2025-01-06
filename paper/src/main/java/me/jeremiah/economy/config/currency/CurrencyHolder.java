package me.jeremiah.economy.config.currency;

import me.jeremiah.economy.currency.Currency;

import java.util.Collection;

public interface CurrencyHolder {

  Currency getDefaultCurrency();
  Collection<Currency> getCurrencies();
  Currency getCurrency(String name);

  void load();

}
