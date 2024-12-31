package me.jeremiah.economy;

import me.jeremiah.economy.config.BasicConfig;
import me.jeremiah.economy.config.Locale;
import me.jeremiah.economy.currency.Currency;
import me.jeremiah.economy.currency.CurrencyHolder;
import me.jeremiah.economy.hooks.VaultHook;
import me.jeremiah.economy.storage.databases.Database;
import org.bukkit.Bukkit;

import java.io.Closeable;
import java.util.logging.Logger;

public class EconomyPlatform implements Closeable {

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

    if (Bukkit.getPluginManager().isPluginEnabled("Vault"))
      vaultHook = new VaultHook(this);

    currencyConfig.getCurrencies().forEach(Currency::register);
  }

  @Override
  public void close() {
    database.close();
    database = null;
    vaultHook.close();
    vaultHook = null;
    currencyConfig.getCurrencies().forEach(Currency::unregister);
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

}
