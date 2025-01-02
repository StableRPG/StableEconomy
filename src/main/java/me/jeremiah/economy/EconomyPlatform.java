package me.jeremiah.economy;

import me.jeremiah.economy.config.BasicConfig;
import me.jeremiah.economy.config.currency.CurrencyHolder;
import me.jeremiah.economy.config.messages.Locale;
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
import java.util.logging.Logger;

public class EconomyPlatform implements Listener, Closeable {

  public static EconomyPlatform of(AbstractEconomyPlugin plugin, BasicConfig pluginConfig, Locale defaultLocale, CurrencyHolder currencyConfig) {
    return new EconomyPlatform(plugin, pluginConfig, defaultLocale, currencyConfig);
  }

  private final AbstractEconomyPlugin plugin;

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

  public void init() {
    config.load();
    defaultLocale.load();
    currencyConfig.load();

    database = Database.of(config);
    currencyConfig.getCurrencies().forEach(Currency::register);

    Bukkit.getPluginManager().registerEvents(this, config.getPlugin());
    if (Bukkit.getPluginManager().isPluginEnabled("Vault"))
      vaultHook = new VaultHook(this);
  }

  @Override
  public void close() {
    vaultHook.close();
    vaultHook = null;
    PlayerJoinEvent.getHandlerList().unregister(this);
    currencyConfig.getCurrencies().forEach(Currency::unregister);
    database.close();
    database = null;
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
