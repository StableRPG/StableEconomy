package me.jeremiah.economy.currency.formatting;

public class CoolFormatter extends CurrencyFormatter {

  private final CommaFormatter comma;
  private final SuffixFormatter suffix;

  public CoolFormatter(String prefix, String suffix) {
    super(prefix, suffix);
    this.comma = new CommaFormatter(prefix, suffix);
    this.suffix = new SuffixFormatter(prefix, suffix);
  }

  @Override
  public String format(double amount) {
    return amount < 1_000_000.0 ? comma.format(amount) : suffix.format(amount);
  }

}
