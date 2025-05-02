package org.stablerpg.stableeconomy;

public final class EconomyPlugin extends AbstractEconomyPlugin {

  @Override
  public void onEnable() {
    initEconomyPlatform();
  }

  @Override
  public void onDisable() {
    closeEconomyPlatform();
  }

}
