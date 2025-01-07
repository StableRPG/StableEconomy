package me.jeremiah.economy.currency.formatting;

import java.text.DecimalFormat;
import java.util.List;

public class SuffixFormatter extends CurrencyFormatter {

  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
  private static final List<String> SUFFIXES = List.of("", "k", "M", "B", "T", "Q", "Qi", "Sx", "Sp", "O", "N", "D");

  public SuffixFormatter(String formatString) {
    super(formatString);
  }

  @Override
  public String format0(double amount) {
    int index;
    for (index = 0; index < SUFFIXES.size() - 1 && amount >= 1000; index++)
      amount /= 1000;
    return DECIMAL_FORMAT.format(amount) + SUFFIXES.get(index);
  }

}
