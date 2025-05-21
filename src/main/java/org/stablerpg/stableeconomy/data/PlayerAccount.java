package org.stablerpg.stableeconomy.data;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.data.util.Dirtyable;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public final class PlayerAccount implements Dirtyable {

  private final EconomyPlatform platform;

  @Getter
  private final UUID uniqueId;
  private final HashMap<String, BalanceEntry> balanceEntries;
  @Getter
  private String username;
  private boolean dirty = false;

  public PlayerAccount(@NotNull EconomyPlatform platform, @NotNull UUID uniqueId, @NotNull String username) {
    this(platform, uniqueId, username, new HashMap<>());
    dirty = true;
  }

  public PlayerAccount(@NotNull EconomyPlatform platform, @NotNull UUID uniqueId, @NotNull String username, @NotNull HashMap<String, BalanceEntry> balanceEntries) {
    this.platform = platform;
    this.uniqueId = uniqueId;
    this.username = username;
    this.balanceEntries = balanceEntries;
  }

  public void updateUsername(String username) {
    this.username = username;
    dirty = true;
  }

  public Collection<BalanceEntry> getBalanceEntries() {
    return balanceEntries.values();
  }

  public double getBalance(@NotNull String currency) {
    return getBalanceEntry(currency).getBalance();
  }

  public BalanceEntry getBalanceEntry(@NotNull String currencyId) {
    if (balanceEntries.containsKey(currencyId))
      return balanceEntries.get(currencyId);
    BalanceEntry entry = platform.getCurrency(currencyId).map(BalanceEntry::new).orElseGet(() -> new BalanceEntry(currencyId));
    balanceEntries.put(currencyId, entry);
    return entry;
  }

  public void setBalance(@NotNull String currency, double balance) {
    getBalanceEntry(currency).setBalance(balance);
  }

  public void addBalance(@NotNull String currency, double balance) {
    getBalanceEntry(currency).addBalance(balance);
  }

  public void subtractBalance(@NotNull String currency, double balance) {
    getBalanceEntry(currency).subtractBalance(balance);
  }

  public boolean hasBalance(@NotNull String currency, double balance) {
    return getBalanceEntry(currency).hasBalance(balance);
  }

  public void resetBalance(@NotNull String currency) {
    getBalanceEntry(currency).resetBalance();
  }

  @Override
  public boolean isDirty() {
    return dirty;
  }

  @Override
  public void markClean() {
    dirty = false;
  }

  @Override
  public boolean equals(@NotNull Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof PlayerAccount playerAccount)) return false;
    return uniqueId.equals(playerAccount.uniqueId);
  }

}
