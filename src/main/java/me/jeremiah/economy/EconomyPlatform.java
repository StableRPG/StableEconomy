package me.jeremiah.economy;

import me.jeremiah.economy.config.BasicConfig;
import me.jeremiah.economy.config.Config;
import me.jeremiah.economy.config.currency.CurrencyConfig;
import me.jeremiah.economy.config.currency.CurrencyHolder;
import me.jeremiah.economy.config.messages.Locale;
import me.jeremiah.economy.config.messages.MessagesConfig;
import me.jeremiah.economy.currency.Currency;
import me.jeremiah.economy.data.databases.Database;
import me.jeremiah.economy.hooks.VaultHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.io.Closeable;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EconomyPlatform implements Listener, Closeable {

  public static EconomyPlatform of(AbstractEconomyPlugin plugin, BasicConfig pluginConfig, Locale defaultLocale, CurrencyHolder currencyConfig) {
    return new EconomyPlatform(plugin, pluginConfig, defaultLocale, currencyConfig);
  }

  private final AbstractEconomyPlugin plugin;

  private ScheduledExecutorService scheduler;

  private final BasicConfig config;
  private final Locale defaultLocale;
  private final CurrencyHolder currencyConfig;

  private Database database;
  private VaultHook vaultHook;

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
    if (Bukkit.getPluginManager().isPluginEnabled("Vault"))
      vaultHook = new VaultHook(this);
  }

  @Override
  public void close() {
    if (vaultHook != null) {
      vaultHook.close();
      vaultHook = null;
    }
    PlayerJoinEvent.getHandlerList().unregister(this);
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

  public ScheduledExecutorService getScheduler() {
    return scheduler;
  }

  public AbstractEconomyPlugin getPlugin() {
    return plugin;
  }

  public Logger getLogger() {
    return plugin.getLogger();
  }

  public BasicConfig getConfig() {
    return config;
  }

  public Locale getDefaultLocale() {
    return defaultLocale;
  }

  public CurrencyHolder getCurrencyConfig() {
    return currencyConfig;
  }

  public Database getDatabase() {
    return database;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public final void onPlayerLoginEvent(PlayerLoginEvent event) {
    Player player = event.getPlayer();
    database.createOrUpdateAccount(player.getUniqueId(), player.getName());
  }

}
