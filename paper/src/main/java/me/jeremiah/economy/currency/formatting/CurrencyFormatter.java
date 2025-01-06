package me.jeremiah.economy.currency.formatting;

public abstract class CurrencyFormatter {

  // TODO: More customizable formatter that allows a string, possibly using MiniMessage

  public static CurrencyFormatter of(Formatters formatter, String prefix, String suffix) {
    return switch (formatter) {
      case COOL -> new CoolFormatter(prefix, suffix);
      case COMMA -> new CommaFormatter(prefix, suffix);
      case SUFFIX -> new SuffixFormatter(prefix, suffix);
      case FAULTY -> new FaultyFormatter(prefix, suffix);
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
