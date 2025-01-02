package me.jeremiah.economy;

import me.jeremiah.economy.config.Config;
import me.jeremiah.economy.config.currency.CurrencyConfig;
import me.jeremiah.economy.config.messages.MessagesConfig;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractEconomyPlugin extends JavaPlugin {

  private EconomyPlatform economyPlatform;

  protected void setEconomyPlatform(EconomyPlatform economyPlatform) {
    this.economyPlatform = economyPlatform;
  }

  protected void initEconomyPlatform() {
    if (economyPlatform == null)
      economyPlatform = EconomyPlatform.of(this, new Config(this), new MessagesConfig(this), new CurrencyConfig(this));
    economyPlatform.init();
  }

  protected void closeEconomyPlatform() {
    if (economyPlatform != null) {
      economyPlatform.close();
      economyPlatform = null;
    }
  }

  public EconomyPlatform getEconomyPlatform() {
    return economyPlatform;
  }

}
