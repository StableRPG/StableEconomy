package org.stablerpg.stableeconomy.config.currency;

import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.currency.Currency;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public final class CurrencyConfig implements CurrencyHolder {

  private final @NotNull EconomyPlatform platform;

  private final @NotNull File currencyDir;
  private final Map<String, Currency> currencies = new HashMap<>();
  private Currency defaultCurrency;

  public CurrencyConfig(@NotNull EconomyPlatform platform) {
    this.platform = platform;
    this.currencyDir = new File(platform.getPlugin().getDataFolder(), "currencies");
  }

  public void load() {
    defaultCurrency = null;
    currencies.clear();

    if (!currencyDir.exists() || !currencyDir.isDirectory()) {
      platform.getPlugin().saveResource("currencies/default/currency.yml", false);
      platform.getPlugin().saveResource("currencies/default/locale.yml", false);
    }

    File[] currencyDirs = currencyDir.listFiles(File::isDirectory);

    if (currencyDirs == null) {
      platform.getLogger().warning("No currency directories found in " + currencyDir.getAbsolutePath() + ". Please ensure the directory exists and contains currency files.");
      return;
    }

    for (File currencyDir : currencyDirs) {
      File currencyFile = new File(currencyDir, "currency.yml");
      File localeFile = new File(currencyDir, "locale.yml");

      Currency.Builder currencyBuilder = new Currency.Builder(currencyDir.getName(), platform)
        .usingYaml(currencyFile);

      if (localeFile.exists()) {
        CurrencyLocale currencyLocale = new CurrencyLocale(platform, localeFile);
        currencyLocale.load();
        currencyBuilder.withLocale(currencyLocale);
      }

      Currency currency = currencyBuilder.build();
      if (currency.isDefaultCurrency())
        defaultCurrency = currency;
      currencies.put(currency.getId(), currency);
    }
  }

  @Override
  public @NotNull Logger getLogger() {
    return platform.getLogger();
  }

  @Override
  public void registerCurrencies() {
    currencies.values().forEach(Currency::register);
  }

  @Override
  public void unregisterCurrencies() {
    currencies.values().forEach(Currency::unregister);
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
  public Optional<Currency> getCurrency(@NotNull String id) {
    return Optional.ofNullable(currencies.get(id));
  }

}
