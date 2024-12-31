package me.jeremiah.economy.currency;

import java.util.Set;

public interface CurrencyHolder {

  Currency getDefaultCurrency();
  Set<Currency> getCurrencies();

  void load();

}
