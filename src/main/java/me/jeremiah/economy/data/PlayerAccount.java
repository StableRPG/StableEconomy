package me.jeremiah.economy.data;

import lombok.Getter;
import me.jeremiah.economy.EconomyPlatform;
import me.jeremiah.economy.data.util.Dirtyable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public final class PlayerAccount implements Dirtyable {

  private final EconomyPlatform platform;

  @Getter
  private final UUID uniqueId;
  @Getter
  private String username;

  private final HashMap<String, BalanceEntry> balanceEntries;

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

  public @NotNull BalanceEntry getBalanceEntry(@NotNull String currencyId) {
    return balanceEntries.computeIfAbsent(currencyId,
      id -> platform.getCurrencyConfig().getCurrency(currencyId)
      .map(BalanceEntry::new)
      .orElse(new BalanceEntry(currencyId))
    );
  }

  public double getBalance(@NotNull String currency) {
    return getBalanceEntry(currency).getBalance();
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
