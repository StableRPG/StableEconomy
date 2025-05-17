package org.stablerpg.stableeconomy;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.stablerpg.stableeconomy.api.EconomyAPI;
import org.stablerpg.stableeconomy.api.PriceProvider;
import org.stablerpg.stableeconomy.config.currency.CurrencyConfig;
import org.stablerpg.stableeconomy.config.currency.CurrencyHolder;
import org.stablerpg.stableeconomy.config.database.DatabaseConfig;
import org.stablerpg.stableeconomy.config.database.DatabaseConfigImpl;
import org.stablerpg.stableeconomy.config.messages.Locale;
import org.stablerpg.stableeconomy.config.messages.MessagesConfig;
import org.stablerpg.stableeconomy.config.prices.PriceConfig;
import org.stablerpg.stableeconomy.config.prices.PriceConfigImpl;
import org.stablerpg.stableeconomy.config.shop.ShopConfig;
import org.stablerpg.stableeconomy.config.shop.ShopConfigImpl;
import org.stablerpg.stableeconomy.data.PlayerAccount;
import org.stablerpg.stableeconomy.data.databases.Database;
import org.stablerpg.stableeconomy.hooks.PlaceholderAPIHook;
import org.stablerpg.stableeconomy.hooks.VaultHook;
import org.stablerpg.stableeconomy.shop.ShopManager;

import java.io.Closeable;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class EconomyPlatform implements EconomyAPI, Listener, Closeable {

  @Getter
  private final AbstractEconomyPlugin plugin;

  @Getter
  private final DatabaseConfig config;
  @Getter
  private final Locale defaultLocale;
  @Getter
  private final CurrencyHolder currencyHolder;
  @Getter
  private final PriceConfig priceConfig;
  @Getter
  private final ShopConfig shopConfig;

  private Database database;

  private VaultHook vaultHook;
  private PlaceholderAPIHook placeholderAPIHook;

  public EconomyPlatform(AbstractEconomyPlugin plugin, DatabaseConfig config, Locale defaultLocale, CurrencyHolder currencyHolder, PriceConfig priceConfig, ShopConfig shopConfig) {
    this.plugin = plugin;
    this.config = config;
    this.defaultLocale = defaultLocale;
    this.currencyHolder = currencyHolder;
    this.priceConfig = priceConfig;
    this.shopConfig = shopConfig;
  }

  public EconomyPlatform(AbstractEconomyPlugin plugin) {
    this.plugin = plugin;
    this.config = new DatabaseConfigImpl(plugin);
    this.defaultLocale = new MessagesConfig(plugin);
    this.currencyHolder = new CurrencyConfig(this);
    this.priceConfig = new PriceConfigImpl(plugin);
    this.shopConfig = new ShopConfigImpl(this);
  }

  public void init() {
    config.load();
    defaultLocale.load();
    currencyHolder.load();
    priceConfig.load();
    shopConfig.load();

    database = Database.of(this);

    Bukkit.getPluginManager().registerEvents(this, plugin);
    loadHooks();
    Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> loadHooks(), 20L);
  }

  private void loadHooks() {
    if (Bukkit.getPluginManager().isPluginEnabled("Vault")) vaultHook = new VaultHook(this);
    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) placeholderAPIHook = new PlaceholderAPIHook(this);
  }

  @Override
  public void close() {
    shopConfig.close();
    priceConfig.close();
    if (placeholderAPIHook != null) {
      placeholderAPIHook.close();
      placeholderAPIHook = null;
    }
    if (vaultHook != null) {
      vaultHook.close();
      vaultHook = null;
    }
    PlayerLoginEvent.getHandlerList().unregister(this);
    currencyHolder.close();
    database.close();
    database = null;
  }

  public Logger getLogger() {
    return plugin.getLogger();
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public final void onPlayerLoginEvent(PlayerLoginEvent event) {
    Player player = event.getPlayer();
    database.createOrUpdateAccount(player.getUniqueId(), player.getName());
  }

  @Override
  public PlayerAccount getAccount(UUID uniqueId) {
    return database.getAccount(uniqueId).join();
  }

  @Override
  public PlayerAccount getAccount(String username) {
    return database.getAccount(username).join();
  }

  @Override
  public double getBalance(UUID uniqueId, String currency) {
    return database.query(uniqueId, account -> account.getBalance(currency)).join();
  }

  @Override
  public double getBalance(String username, String currency) {
    return database.query(username, account -> account.getBalance(currency)).join();
  }

  @Override
  public void setBalance(UUID uniqueId, double amount, String currency) {
    database.update(uniqueId, account -> account.setBalance(currency, amount));
  }

  @Override
  public void setBalance(String username, double amount, String currency) {
    database.update(username, account -> account.setBalance(currency, amount));
  }

  @Override
  public void addBalance(UUID uniqueId, double amount, String currency) {
    database.update(uniqueId, account -> account.addBalance(currency, amount));
  }

  @Override
  public void addBalance(String username, double amount, String currency) {
    database.update(username, account -> account.addBalance(currency, amount));
  }

  @Override
  public void subtractBalance(UUID uniqueId, double amount, String currency) {
    database.update(uniqueId, account -> account.subtractBalance(currency, amount));
  }

  @Override
  public void subtractBalance(String username, double amount, String currency) {
    database.update(username, account -> account.subtractBalance(currency, amount));
  }

  @Override
  public void resetBalance(UUID uniqueId, String currency) {
    database.update(uniqueId, account -> account.resetBalance(currency));
  }

  @Override
  public void resetBalance(String username, String currency) {
    database.update(username, account -> account.resetBalance(currency));
  }

  @Override
  public List<PlayerAccount> getLeaderboard(String currency) {
    return database.sortedByBalance(currency);
  }

  @Override
  public PriceProvider getPriceProvider() {
    return priceConfig.getPriceProvider();
  }

  @Override
  public ShopManager getShopManager() {
    return shopConfig.getShopManager();
  }

}
