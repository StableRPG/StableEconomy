package org.stablerpg.stableeconomy.data;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.currency.Currency;
import org.stablerpg.stableeconomy.data.util.Dirtyable;

public final class BalanceEntry implements Comparable<BalanceEntry>, Dirtyable {

  @Getter
  private final String currency;
  private double savedBalance;
  @Getter
  private double unsavedBalance = 0;

  public BalanceEntry(Currency currency) {
    this(currency.getId());
    this.unsavedBalance = currency.getStartingBalance();
  }

  public BalanceEntry(String currency, double savedBalance) {
    this.currency = currency;
    this.savedBalance = savedBalance;
  }

  public BalanceEntry(String currency) {
    this(currency, 0);
  }

  public void addBalance(double balance) {
    unsavedBalance += balance;
  }

  public double getBalance() {
    return savedBalance + unsavedBalance;
  }

  public void subtractBalance(double balance) {
    unsavedBalance -= balance;
  }

  public void setBalance(double balance) {
    unsavedBalance += balance - getBalance();
  }

  public boolean hasBalance(double amount) {
    return getBalance() >= amount;
  }

  public void resetBalance() {
    unsavedBalance = -savedBalance;
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
