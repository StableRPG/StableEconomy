package me.jeremiah.stableeconomy.currency.formatting;

import java.text.DecimalFormat;

public class CommaFormatter extends CurrencyFormatter {

  private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###.##");

  public CommaFormatter(String formatString) {
    super(formatString);
  }

  @Override
  protected String format0(double amount) {
    return COMMA_FORMAT.format(amount);
  }

}
