package org.stablerpg.stableeconomy.currency.formatting;

public abstract class CurrencyFormatter {

  public static CurrencyFormatter of(Formatters formatter, String formatString) {
    return switch (formatter) {
      case COOL -> new CoolFormatter(formatString);
      case COMMA -> new CommaFormatter(formatString);
      case SUFFIX -> new SuffixFormatter(formatString);
      case FAULTY -> new FaultyFormatter(formatString);
    };
  }

  protected final String formatString;

  public CurrencyFormatter(String formatString) {
    this.formatString = formatString.replaceAll("<amount>", "%s");
  }

  public String format(double amount) {
    return formatString.formatted(format0(amount));
  }

  protected abstract String format0(double amount);

}
