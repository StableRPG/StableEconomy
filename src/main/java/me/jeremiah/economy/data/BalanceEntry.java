package me.jeremiah.economy.data;

import me.jeremiah.economy.currency.Currency;
import me.jeremiah.economy.data.util.Dirtyable;
import org.jetbrains.annotations.NotNull;

public final class BalanceEntry implements Comparable<BalanceEntry>, Dirtyable {

  private final String currency;
  private double savedBalance;
  private double unsavedBalance = 0;

  public BalanceEntry(Currency currency) {
    this(currency.getId(), currency.getStartingBalance());
  }

  public BalanceEntry(String currency) {
    this(currency, 0);
  }

  public BalanceEntry(String currency, double savedBalance) {
    this.currency = currency;
    this.savedBalance = savedBalance;
  }

  public String getCurrency() {
    return currency;
  }

  public double getBalance() {
    return savedBalance + unsavedBalance;
  }

  public double getUnsavedBalance() {
    return unsavedBalance;
  }

  public void setBalance(double balance) {
    unsavedBalance += balance - getBalance();
  }

  public void addBalance(double balance) {
    unsavedBalance += balance;
  }

  public void subtractBalance(double balance) {
    unsavedBalance -= balance;
  }

  @Override
  public int compareTo(@NotNull BalanceEntry balanceEntry) {
    return Double.compare(getBalance(), balanceEntry.getBalance());
  }

  @Override
  public boolean isDirty() {
    return unsavedBalance != 0;
  }

  @Override
  public void markClean() {
    savedBalance += unsavedBalance;
    unsavedBalance = 0;
  }
}
