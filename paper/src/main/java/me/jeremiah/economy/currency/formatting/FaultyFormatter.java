package me.jeremiah.economy.currency.formatting;

public class FaultyFormatter extends CurrencyFormatter {

  public FaultyFormatter(String prefix, String suffix) {
    super(prefix, suffix);
  }

  @Override
  public String format(double amount) {
    return prefix + amount + suffix;
  }

}
