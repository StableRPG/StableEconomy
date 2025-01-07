package me.jeremiah.economy.currency.formatting;

public class FaultyFormatter extends CurrencyFormatter {

  public FaultyFormatter(String formatString) {
    super(formatString);
  }

  @Override
  public String format0(double amount) {
    return String.valueOf(amount);
  }

}
