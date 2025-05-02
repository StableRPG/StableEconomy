package me.jeremiah.stableeconomy.currency.formatting;

public class FaultyFormatter extends CurrencyFormatter {

  public FaultyFormatter(String formatString) {
    super(formatString);
  }

  @Override
  protected String format0(double amount) {
    return String.valueOf(amount);
  }

}
