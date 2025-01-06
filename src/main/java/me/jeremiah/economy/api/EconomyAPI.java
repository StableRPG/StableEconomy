package me.jeremiah.economy.api;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

public interface EconomyAPI {

  default double getBalance(OfflinePlayer player) {
    return getBalance(player, "default");
  }

  default double getBalance(OfflinePlayer player, String currency) {
    return getBalance(player.getUniqueId(), currency);
  }

  default double getBalance(UUID uniqueId) {
    return getBalance(uniqueId, "default");
  }

  double getBalance(UUID uniqueId, String currency);

  default void setBalance(OfflinePlayer player, double amount) {
    setBalance(player, amount, "default");
  }

  default void setBalance(OfflinePlayer player, double amount, String currency) {
    setBalance(player.getUniqueId(), amount, currency);
  }

  default void setBalance(UUID uniqueId, double amount) {
    setBalance(uniqueId, amount, "default");
  }

  void setBalance(UUID uniqueId, double amount, String currency);

  default void addBalance(OfflinePlayer player, double amount) {
    addBalance(player, amount, "default");
  }

  default void addBalance(OfflinePlayer player, double amount, String currency) {
    addBalance(player.getUniqueId(), amount, currency);
  }

  default void addBalance(UUID uniqueId, double amount) {
    addBalance(uniqueId, amount, "default");
  }

  void addBalance(UUID uniqueId, double amount, String currency);

  default void subtractBalance(OfflinePlayer player, double amount) {
    subtractBalance(player, amount, "default");
  }

  default void subtractBalance(OfflinePlayer player, double amount, String currency) {
    subtractBalance(player.getUniqueId(), amount, currency);
  }

  default void subtractBalance(UUID uniqueId, double amount) {
    subtractBalance(uniqueId, amount, "default");
  }

  void subtractBalance(UUID uniqueId, double amount, String currency);

  default boolean hasBalance(OfflinePlayer player, double amount) {
    return hasBalance(player, amount, "default");
  }

  default boolean hasBalance(OfflinePlayer player, double amount, String currency) {
    return hasBalance(player.getUniqueId(), amount, currency);
  }

  default boolean hasBalance(UUID uniqueId, double amount) {
    return hasBalance(uniqueId, amount, "default");
  }

  default boolean hasBalance(UUID uniqueId, double amount, String currency) {
    return getBalance(uniqueId, currency) >= amount;
  }

}
