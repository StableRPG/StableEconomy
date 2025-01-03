package me.jeremiah.economy.currency.formatting;

public abstract class CurrencyFormatter {

  public static CurrencyFormatter of(String formatter, String prefix, String suffix) {
    return switch (formatter) {
      case "cool" -> new CoolFormatter(prefix, suffix);
      case "comma" -> new CommaFormatter(prefix, suffix);
      case "suffix" -> new SuffixFormatter(prefix, suffix);
      case "none" -> new FaultyFormatter(prefix, suffix);
      default -> new FaultyFormatter(prefix, suffix);
    };
  }

  protected final String prefix;
  protected final String suffix;

  public CurrencyFormatter(String prefix, String suffix) {
    this.prefix = prefix;
    this.suffix = suffix;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getSuffix() {
    return suffix;
  }

  public abstract String format(double amount);

}
