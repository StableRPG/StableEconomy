package me.jeremiah.economy.config;

import me.jeremiah.economy.AbstractEconomyPlugin;
import me.jeremiah.economy.EconomyPlatform;
import me.jeremiah.economy.currency.Currency;
import me.jeremiah.economy.currency.CurrencyHolder;
import me.jeremiah.economy.currency.CurrencyLocale;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public final class CurrencyConfig implements CurrencyHolder {

  private final @NotNull AbstractEconomyPlugin plugin;

  private final @NotNull File currencyDir;

  private Currency defaultCurrency;
  private final Set<Currency> currencies = new HashSet<>();

  public CurrencyConfig(@NotNull AbstractEconomyPlugin plugin) {
    this.plugin = plugin;
    currencyDir = new File(plugin.getDataFolder(), "currencies");
  }

  public void load() {
    final EconomyPlatform platform = plugin.getEconomyPlatform();

    defaultCurrency = null;
    currencies.clear();

    if (!currencyDir.exists() || !currencyDir.isDirectory()) {
      setupDefaultCurrency(platform, defaultCurrency);
      return;
    }

    File[] currencyDirs = currencyDir.listFiles(File::isDirectory);

    if (currencyDirs == null) {
      setupDefaultCurrency(platform, defaultCurrency);
      return;
    }

    for (File currencyDir : currencyDirs) {
      File currencyFile = new File(currencyDir, "currency.yml");
      File localeFile = new File(currencyDir, "locale.yml");

      Currency.Builder currencyBuilder = new Currency.Builder(currencyDir.getName().toLowerCase(), platform);

      currencyBuilder.usingYaml(YamlConfiguration.loadConfiguration(currencyFile));

      if (localeFile.exists())
        currencyBuilder.withLocale(CurrencyLocale.of(plugin, localeFile));

      Currency currency = currencyBuilder.build();
      if (currency.isDefaultCurrency())
        setupDefaultCurrency(platform, currency);
      currencies.add(currency);
    }
  }

  private void setupDefaultCurrency(@NotNull EconomyPlatform platform, @Nullable Currency currency) {
    if (currency == null)
      currency = new Currency.Builder("default", platform)
        .withLocale(platform.getDefaultLocale())
        .withViewCommandName("balance")
        .withViewCommandAliases("bal")
        .withTransferCommandName("pay")
        .withLeaderboardCommandName("balancetop")
        .withLeaderboardCommandAliases("baltop")
        .withAdminCommandName("economy")
        .withAdminCommandAliases("eco")
        .withAdminCommandPermission("economy.admin")
        .build();

    defaultCurrency = currency;
  }

  @Override
  public Currency getDefaultCurrency() {
    return defaultCurrency;
  }

  @Override
  public Set<Currency> getCurrencies() {
    return Set.copyOf(currencies);
  }

}
