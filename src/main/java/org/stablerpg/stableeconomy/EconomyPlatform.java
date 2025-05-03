package org.stablerpg.stableeconomy;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.stablerpg.stableeconomy.api.EconomyAPI;
import org.stablerpg.stableeconomy.config.BasicConfig;
import org.stablerpg.stableeconomy.config.Config;
import org.stablerpg.stableeconomy.config.currency.CurrencyConfig;
import org.stablerpg.stableeconomy.config.currency.CurrencyHolder;
import org.stablerpg.stableeconomy.config.messages.Locale;
import org.stablerpg.stableeconomy.config.messages.MessagesConfig;
import org.stablerpg.stableeconomy.currency.Currency;
import org.stablerpg.stableeconomy.data.databases.Database;
import org.stablerpg.stableeconomy.hooks.PlaceholderAPIHook;
import org.stablerpg.stableeconomy.hooks.VaultHook;

import java.io.Closeable;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EconomyPlatform implements EconomyAPI, Listener, Closeable {

  @Getter
  private final AbstractEconomyPlugin plugin;

  @Getter
  private ScheduledExecutorService scheduler;

  @Getter
  private final BasicConfig config;
  @Getter
  private final Locale defaultLocale;
  @Getter
  private final CurrencyHolder currencyConfig;

  @Getter
  private Database database;

  private VaultHook vaultHook;
  private PlaceholderAPIHook placeholderAPIHook;

  public EconomyPlatform(AbstractEconomyPlugin plugin, BasicConfig config, Locale defaultLocale, CurrencyHolder currencyConfig) {
    this.plugin = plugin;
    this.config = config;
    this.defaultLocale = defaultLocale;
    this.currencyConfig = currencyConfig;
  }

  public EconomyPlatform(AbstractEconomyPlugin plugin) {
    this.plugin = plugin;
    this.config = new Config(plugin);
    this.defaultLocale = new MessagesConfig(plugin);
    this.currencyConfig = new CurrencyConfig(this,  new File(plugin.getDataFolder(), "currencies"));
  }

  public void init() {
    config.load();
    defaultLocale.load();
    currencyConfig.load();

    scheduler = Executors.newSingleThreadScheduledExecutor();
    database = Database.of(this);
    currencyConfig.getCurrencies().forEach(Currency::register);

    Bukkit.getPluginManager().registerEvents(this, plugin);
    loadHooks();
    Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> loadHooks(), 20L);
  }

  private void loadHooks() {
    if (Bukkit.getPluginManager().isPluginEnabled("Vault"))
      vaultHook = new VaultHook(this);
    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
      placeholderAPIHook = new PlaceholderAPIHook(this);
  }

  @Override
  public void close() {
    if (placeholderAPIHook != null) {
      placeholderAPIHook.close();
      placeholderAPIHook = null;
    }
    if (vaultHook != null) {
      vaultHook.close();
      vaultHook = null;
    }
    PlayerLoginEvent.getHandlerList().unregister(this);
    currencyConfig.getCurrencies().forEach(Currency::unregister);
    database.close();
    database = null;
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS))
        scheduler.shutdownNow();
    } catch (InterruptedException exception) {
      getLogger().log(Level.SEVERE, "Failed to shutdown scheduler", exception);
      scheduler.shutdownNow();
    }
    scheduler = null;
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
  public double getBalance(UUID uniqueId, String currency) {
    return database.getByUUID(uniqueId).map(account -> account.getBalance(currency)).orElse(0.0);
  }

  @Override
  public void setBalance(UUID uniqueId, double amount, String currency) {
    database.updateByUUID(uniqueId, account -> account.setBalance(currency, amount));
  }

  @Override
  public void addBalance(UUID uniqueId, double amount, String currency) {
    database.updateByUUID(uniqueId, account -> account.addBalance(currency, amount));
  }

  @Override
  public void subtractBalance(UUID uniqueId, double amount, String currency) {
    database.updateByUUID(uniqueId, account -> account.subtractBalance(currency, amount));
  }

}
