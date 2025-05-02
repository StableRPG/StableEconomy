package me.jeremiah.economy;

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
