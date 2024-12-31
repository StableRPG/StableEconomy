package me.jeremiah.economy;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import me.jeremiah.economy.config.Config;
import me.jeremiah.economy.config.CurrencyConfig;
import me.jeremiah.economy.config.MessagesConfig;

public final class EconomyPlugin extends AbstractEconomyPlugin {

  private EconomyPlatform platform;

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

    platform = EconomyPlatform.of(this, new Config(this), new MessagesConfig(this), new CurrencyConfig(this));
    platform.init();
  }

  @Override
  public void onDisable() {
    CommandAPI.onDisable();
    platform.close();
  }

  public EconomyPlatform getEconomyPlatform() {
    return platform;
  }

}
