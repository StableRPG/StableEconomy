package org.stablerpg.stableeconomy;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;

public final class StableEconomy extends AbstractEconomyPlugin {

  @Override
  public void onLoad() {
    CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(true).shouldHookPaperReload(true).useLatestNMSVersion(true));
  }

  @Override
  public void onEnable() {
    CommandAPI.onEnable();
    initEconomyPlatform();
  }

  @Override
  public void onDisable() {
    closeEconomyPlatform();
    CommandAPI.onDisable();
  }

}
