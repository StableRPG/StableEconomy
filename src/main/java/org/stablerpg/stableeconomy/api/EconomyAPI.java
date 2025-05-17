package org.stablerpg.stableeconomy.api;

import org.bukkit.OfflinePlayer;
import org.stablerpg.stableeconomy.StableEconomy;
import org.stablerpg.stableeconomy.data.PlayerAccount;
import org.stablerpg.stableeconomy.shop.AbstractShopManager;

import java.util.List;
import java.util.UUID;

public interface EconomyAPI {

  static EconomyAPI get() {
    StableEconomy plugin = StableEconomy.getPlugin(StableEconomy.class);
    if (plugin == null) {
      throw new IllegalStateException("EconomyPlugin is not initialized. Please ensure the plugin is enabled.");
    }
    EconomyAPI api = plugin.getEconomyAPI();
    if (api == null) {
      throw new IllegalStateException("EconomyAPI is not initialized. Please ensure the plugin has been properly initialized.");
    }
    return api;
  }

  default PlayerAccount getAccount(OfflinePlayer player) {
    return getAccount(player.getUniqueId());
  }

  PlayerAccount getAccount(UUID uniqueId);

  PlayerAccount getAccount(String username);

  default double getBalance(OfflinePlayer player) {
    return getBalance(player, "default");
  }

  default double getBalance(OfflinePlayer player, String currency) {
    return getBalance(player.getUniqueId(), currency);
  }

  double getBalance(UUID uniqueId, String currency);

  default double getBalance(UUID uniqueId) {
    return getBalance(uniqueId, "default");
  }

  default double getBalance(String username) {
    return getBalance(username, "default");
  }

  double getBalance(String username, String currency);

  default void setBalance(OfflinePlayer player, double amount) {
    setBalance(player, amount, "default");
  }

  default void setBalance(OfflinePlayer player, double amount, String currency) {
    setBalance(player.getUniqueId(), amount, currency);
  }

  void setBalance(UUID uniqueId, double amount, String currency);

  default void setBalance(UUID uniqueId, double amount) {
    setBalance(uniqueId, amount, "default");
  }

  default void setBalance(String username, double amount) {
    setBalance(username, amount, "default");
  }

  void setBalance(String username, double amount, String currency);

  default void addBalance(OfflinePlayer player, double amount) {
    addBalance(player, amount, "default");
  }

  default void addBalance(OfflinePlayer player, double amount, String currency) {
    addBalance(player.getUniqueId(), amount, currency);
  }

  void addBalance(UUID uniqueId, double amount, String currency);

  default void addBalance(UUID uniqueId, double amount) {
    addBalance(uniqueId, amount, "default");
  }

  default void addBalance(String username, double amount) {
    addBalance(username, amount, "default");
  }

  void addBalance(String username, double amount, String currency);

  default void subtractBalance(OfflinePlayer player, double amount) {
    subtractBalance(player, amount, "default");
  }

  default void subtractBalance(OfflinePlayer player, double amount, String currency) {
    subtractBalance(player.getUniqueId(), amount, currency);
  }

  void subtractBalance(UUID uniqueId, double amount, String currency);

  default void subtractBalance(UUID uniqueId, double amount) {
    subtractBalance(uniqueId, amount, "default");
  }

  default void subtractBalance(String username, double amount) {
    subtractBalance(username, amount, "default");
  }

  void subtractBalance(String username, double amount, String currency);

  default boolean hasBalance(OfflinePlayer player, double amount) {
    return hasBalance(player, amount, "default");
  }

  default boolean hasBalance(OfflinePlayer player, double amount, String currency) {
    return hasBalance(player.getUniqueId(), amount, currency);
  }

  default boolean hasBalance(UUID uniqueId, double amount, String currency) {
    return getBalance(uniqueId, currency) >= amount;
  }

  default boolean hasBalance(UUID uniqueId, double amount) {
    return hasBalance(uniqueId, amount, "default");
  }

  default boolean hasBalance(String username, double amount) {
    return hasBalance(username, amount, "default");
  }

  default boolean hasBalance(String username, double amount, String currency) {
    return getBalance(username, currency) >= amount;
  }

  default void resetBalance(OfflinePlayer player) {
    resetBalance(player.getUniqueId());
  }

  default void resetBalance(UUID uniqueId) {
    resetBalance(uniqueId, "default");
  }

  void resetBalance(UUID uniqueId, String currency);

  default void resetBalance(OfflinePlayer player, String currency) {
    resetBalance(player.getUniqueId(), currency);
  }

  default void resetBalance(String username) {
    resetBalance(username, "default");
  }

  void resetBalance(String username, String currency);

  List<PlayerAccount> getLeaderboard(String currency);

  PriceProvider getPriceProvider();

  default AbstractShopManager getShopManager() {
    return null;
  }

}
