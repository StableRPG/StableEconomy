package me.jeremiah.economy.config.currency;

import me.jeremiah.economy.EconomyPlatform;
import me.jeremiah.economy.currency.Currency;
import me.jeremiah.economy.currency.formatting.Formatters;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CurrencyConfig implements CurrencyHolder {

  private final @NotNull EconomyPlatform platform;

  private final @NotNull File currencyDir;

  private Currency defaultCurrency;
  private final Map<String, Currency> currencies = new HashMap<>();

  public CurrencyConfig(@NotNull EconomyPlatform platform, @NotNull File currencyDir) {
    this.platform = platform;
    this.currencyDir = currencyDir;
  }

  public void load() {

    defaultCurrency = null;
    currencies.clear();

    if (!currencyDir.exists() || !currencyDir.isDirectory()) {
      setupDefaultCurrency(platform, null);
      return;
    }

    File[] currencyDirs = currencyDir.listFiles(File::isDirectory);

    if (currencyDirs == null) {
      setupDefaultCurrency(platform, null);
      return;
    }

    for (File currencyDir : currencyDirs) {
      File currencyFile = new File(currencyDir, "currency.yml");
      File localeFile = new File(currencyDir, "locale.yml");

      Currency.Builder currencyBuilder = new Currency.Builder(currencyDir.getName().toLowerCase(), platform)
        .usingYaml(YamlConfiguration.loadConfiguration(currencyFile));

      if (localeFile.exists())
        currencyBuilder.withLocale(CurrencyLocale.of(platform, localeFile));

      Currency currency = currencyBuilder.build();
      if (currency.isDefaultCurrency())
        setupDefaultCurrency(platform, currency);
      currencies.put(currency.getId(), currency);
    }

    if (defaultCurrency == null)
      setupDefaultCurrency(platform, null);
  }

  private void setupDefaultCurrency(@NotNull EconomyPlatform platform, @Nullable Currency currency) {
    if (currency == null) {
      currency = new Currency.Builder("default", platform)
        .withLocale(platform.getDefaultLocale())
        .withDisplayName("Dollar", "Dollars")
        .withPrefix("$")
        .withFormatter(Formatters.COOL)
        .withViewCommandName("balance")
        .withViewCommandAliases("bal")
        .withTransferCommandName("pay")
        .withLeaderboardCommandName("balancetop")
        .withLeaderboardCommandAliases("baltop")
        .withAdminCommandName("economy")
        .withAdminCommandAliases("eco")
        .withAdminCommandPermission("economy.admin")
        .build();
      currencies.put("default", currency);
    }

    defaultCurrency = currency;
  }

  @Override
  public Currency getDefaultCurrency() {
    return defaultCurrency;
  }

  @Override
  public Collection<Currency> getCurrencies() {
    return Collections.unmodifiableCollection(currencies.values());
  }

  @Override
  public Currency getCurrency(@NotNull String id) {
    return currencies.get(id);
  }

}
