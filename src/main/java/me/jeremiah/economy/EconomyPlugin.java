package me.jeremiah.economy;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;

public final class EconomyPlugin extends AbstractEconomyPlugin {

  @Override
  public void onLoad() {
    CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
      .usePluginNamespace()
      .shouldHookPaperReload(true)
    );
  }

  @Override
  public void onEnable() {
    CommandAPI.onEnable();
    initEconomyPlatform();
  }

  @Override
  public void onDisable() {
    CommandAPI.onDisable();
    closeEconomyPlatform();
  }

}
