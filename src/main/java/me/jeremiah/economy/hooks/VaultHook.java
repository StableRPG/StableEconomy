package me.jeremiah.economy.hooks;

import me.jeremiah.economy.EconomyPlatform;
import me.jeremiah.economy.currency.Currency;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;

public final class VaultHook implements Economy, Closeable {

  private final EconomyPlatform platform;
  private final Currency currency;

  public VaultHook(EconomyPlatform platform) {
    this.platform = platform;
    this.currency = platform.getCurrencyConfig().getDefaultCurrency();
    Bukkit.getServicesManager().register(Economy.class, this, platform.getPlugin(), ServicePriority.Highest);
    RegisteredServiceProvider<Economy> registeredServiceProvider = Bukkit.getServicesManager().getRegistration(Economy.class);

    if (registeredServiceProvider == null)
      platform.getLogger().severe("Failed to register with net.milkbowl.vault.economy.Economy.class");
  }

  @Override
  public boolean isEnabled() {
    RegisteredServiceProvider<Economy> service = Bukkit.getServicesManager().getRegistration(Economy.class);
    return service != null && service.getPlugin() == platform.getPlugin();
  }

  @Override
  public String getName() {
    return platform.getPlugin().getName();
  }

  @Override
  public int fractionalDigits() {
    return 2;
  }

  @Override
  public String format(double amount) {
    return currency.format(amount);
  }

  @Override
  public String currencyNameSingular() {
    return "Dollar";
  }

  @Override
  public String currencyNamePlural() {
    return "Dollars";
  }

  @Override
  public boolean hasAccount(OfflinePlayer player) {
    return platform.getDatabase().getByPlayer(player).isPresent();
  }

  @Override
  public boolean hasAccount(OfflinePlayer player, String worldName) {
    return hasAccount(player);
  }

  @Override
  public boolean hasAccount(String player) {
    platform.getLogger().warning("Checking for an account by name is not recommended. Please use the UUID of the player instead.");
    return platform.getDatabase().getByUsername(player).isPresent();
  }

  @Override
  public boolean hasAccount(String player, String worldName) {
    return hasAccount(player);
  }

  @Override
  public double getBalance(OfflinePlayer player) {
    return currency.getBalance(player);
  }

  @Override
  public double getBalance(OfflinePlayer player, String world) {
    return getBalance(player);
  }

  @Override
  public double getBalance(String player) {
    platform.getLogger().warning("Getting the balance of a player by name is not recommended. Please use the UUID of the player instead.");
    return currency.getBalance(player);
  }

  @Override
  public double getBalance(String player, String world) {
    return getBalance(player);
  }

  @Override
  public boolean has(OfflinePlayer player, double amount) {
    return getBalance(player) >= amount;
  }

  @Override
  public boolean has(OfflinePlayer player, String worldName, double amount) {
    return has(player, amount);
  }

  @Override
  public boolean has(String player, double amount) {
    platform.getLogger().warning("Checking if a player has a certain amount by name is not recommended. Please use the UUID of the player instead.");
    return getBalance(player) >= amount;
  }

  @Override
  public boolean has(String player, String worldName, double amount) {
    return has(player, amount);
  }

  @Override
  public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
    if (player == null)
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player cannot be null.");
    if (amount < 0)
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw a negative amount.");
    if (!hasAccount(player))
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player doesn't exist.");
    if (!has(player, amount))
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player doesn't have funds.");
    currency.subtractBalance(player, amount);
    return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
  }

  @Override
  public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
    return withdrawPlayer(player, amount);
  }

  @Override
  public EconomyResponse withdrawPlayer(String player, double amount) {
    platform.getLogger().warning("Withdrawing from a player by name is not recommended. Please use the UUID of the player instead.");
    if (player == null)
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player cannot be null.");
    if (amount < 0)
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw a negative amount.");
    if (!hasAccount(player))
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player doesn't exist.");
    if (!has(player, amount))
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player doesn't have funds.");
    currency.subtractBalance(player, amount);
    return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
  }

  @Override
  public EconomyResponse withdrawPlayer(String player, String worldName, double amount) {
    return withdrawPlayer(player, amount);
  }

  @Override
  public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
    if (player == null)
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player cannot be null.");
    if (amount < 0)
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit a negative amount.");
    if (!hasAccount(player))
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player doesn't exist.");
    currency.addBalance(player, amount);
    return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
  }

  @Override
  public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
    return depositPlayer(player, amount);
  }

  @Override
  public EconomyResponse depositPlayer(String player, double amount) {
    platform.getLogger().warning("Depositing to a player by name is not recommended. Please use the UUID of the player instead.");
    if (player == null)
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player cannot be null.");
    if (amount < 0)
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit a negative amount.");
    if (!hasAccount(player))
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player doesn't exist.");
    currency.addBalance(player, amount);
    return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
  }

  @Override
  public EconomyResponse depositPlayer(String player, String worldName, double amount) {
    return depositPlayer(player, amount);
  }

  @Override
  public boolean createPlayerAccount(OfflinePlayer player) {
    return hasAccount(player);
  }

  @Override
  public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
    return createPlayerAccount(player);
  }

  @Override
  public boolean createPlayerAccount(String player) {
    platform.getLogger().warning("Creating an account by name is not recommended. Please use the UUID of the player instead.");
    return hasAccount(player);
  }

  @Override
  public boolean createPlayerAccount(String player, String worldName) {
    return createPlayerAccount(player);
  }

  @Override
  public boolean hasBankSupport() {
    return false;
  }

  @Override
  public EconomyResponse createBank(String name, String player) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }

  @Override
  public EconomyResponse createBank(String name, OfflinePlayer player) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }

  @Override
  public EconomyResponse deleteBank(String name) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }

  @Override
  public EconomyResponse bankBalance(String name) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }

  @Override
  public EconomyResponse bankHas(String name, double amount) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }

  @Override
  public EconomyResponse bankWithdraw(String name, double amount) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }

  @Override
  public EconomyResponse bankDeposit(String name, double amount) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }

  @Override
  public EconomyResponse isBankOwner(String name, String player) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }

  @Override
  public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }

  @Override
  public EconomyResponse isBankMember(String name, String player) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }

  @Override
  public EconomyResponse isBankMember(String name, OfflinePlayer player) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }

  @Override
  public List<String> getBanks() {
    return Collections.emptyList();
  }

  @Override
  public void close() {
    Bukkit.getServicesManager().unregister(Economy.class, this);
  }

}