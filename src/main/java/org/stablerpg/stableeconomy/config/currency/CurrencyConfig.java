package org.stablerpg.stableeconomy.config.currency;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.currency.Currency;
import org.stablerpg.stableeconomy.currency.formatting.Formatters;

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

      Currency.Builder currencyBuilder = new Currency.Builder(currencyDir.getName().toLowerCase(), platform).usingYaml(YamlConfiguration.loadConfiguration(currencyFile));

      if (localeFile.exists()) currencyBuilder.withLocale(CurrencyLocale.of(platform, localeFile));

      Currency currency = currencyBuilder.build();
      if (currency.isDefaultCurrency()) setupDefaultCurrency(platform, currency);
      currencies.put(currency.getId(), currency);
    }

    if (defaultCurrency == null) setupDefaultCurrency(platform, null);
  }

  private void setupDefaultCurrency(@NotNull EconomyPlatform platform, @Nullable Currency currency) {
    if (currency == null) {
      currency = new Currency.Builder("default", platform).withLocale(platform.getDefaultLocale()).withDisplayName("Dollar", "Dollars").withFormattingString("$<amount>").withFormatter(Formatters.COOL).withViewCommandName("balance").withViewCommandAliases("bal").withTransferCommandName("pay").withLeaderboardCommandName("balancetop").withLeaderboardCommandAliases("baltop").withAdminCommandName("economy").withAdminCommandAliases("eco").withAdminCommandPermission("economy.admin").build();
      currencies.put("default", currency);
    }

    defaultCurrency = currency;
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
