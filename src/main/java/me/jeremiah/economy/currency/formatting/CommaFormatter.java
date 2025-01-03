package me.jeremiah.economy.currency.formatting;

import java.text.DecimalFormat;

public class CommaFormatter extends CurrencyFormatter {

  private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###.##");

  public CommaFormatter(String prefix, String suffix) {
    super(prefix, suffix);
  }

  @Override
  public String format(double amount) {
    return prefix + COMMA_FORMAT.format(amount) + suffix;
  }

}
