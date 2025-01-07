package me.jeremiah.economy.currency.formatting;

public class CoolFormatter extends CurrencyFormatter {

  private final CommaFormatter comma;
  private final SuffixFormatter suffix;

  public CoolFormatter(String formatString) {
    super(formatString);
    comma = new CommaFormatter(formatString);
    suffix = new SuffixFormatter(formatString);
  }

  @Override
  public String format0(double amount) {
    return amount < 1_000_000.0 ? comma.format0(amount) : suffix.format0(amount);
  }

}
