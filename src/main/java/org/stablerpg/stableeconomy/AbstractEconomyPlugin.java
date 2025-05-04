package org.stablerpg.stableeconomy;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.stablerpg.stableeconomy.api.EconomyAPI;

@Getter
public abstract class AbstractEconomyPlugin extends JavaPlugin {

  private EconomyPlatform economyPlatform;

  protected void setEconomyPlatform(EconomyPlatform economyPlatform) {
    this.economyPlatform = economyPlatform;
  }

  protected void initEconomyPlatform() {
    if (economyPlatform == null) economyPlatform = new EconomyPlatform(this);
    economyPlatform.init();
  }

  protected void closeEconomyPlatform() {
    if (economyPlatform != null) {
      economyPlatform.close();
      economyPlatform = null;
    }
  }

  public EconomyAPI getEconomyAPI() {
    return economyPlatform;
  }

}
