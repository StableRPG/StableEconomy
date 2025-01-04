package me.jeremiah.economy;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractEconomyPlugin extends JavaPlugin {

  private EconomyPlatform economyPlatform;

  protected void setEconomyPlatform(EconomyPlatform economyPlatform) {
    this.economyPlatform = economyPlatform;
  }

  protected void initEconomyPlatform() {
    if (economyPlatform == null)
      economyPlatform = new EconomyPlatform(this);
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
