package me.jeremiah.economy;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import me.jeremiah.economy.config.messages.messages.AbstractMessage;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public final class EconomyPlugin extends AbstractEconomyPlugin {

  @Override
  public void onLoad() {
    CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
      .usePluginNamespace()
      .shouldHookPaperReload(true)
    );
    ConfigurationSerialization.registerClass(AbstractMessage.class);
  }

  @Override
  public void onEnable() {
    CommandAPI.onEnable();
    initEconomyPlatform();
  }

  @Override
  public void onDisable() {
    closeEconomyPlatform();
    ConfigurationSerialization.unregisterClass(AbstractMessage.class);
    CommandAPI.onDisable();
  }

}
