package me.jeremiah.economy.config.currency;

import me.jeremiah.economy.AbstractEconomyPlugin;
import me.jeremiah.economy.config.messages.Locale;
import me.jeremiah.economy.config.messages.MessageType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;

public class CurrencyLocale implements Locale {

  public static CurrencyLocale of(AbstractEconomyPlugin plugin, File localeFile) {
    return new CurrencyLocale(plugin, localeFile);
  }

  private final AbstractEconomyPlugin plugin;

  private final File localeFile;
  private final HashMap<MessageType, String> messages = new HashMap<>();

  private CurrencyLocale(AbstractEconomyPlugin plugin, File localeFile) {
    this.plugin = plugin;
    this.localeFile = localeFile;
  }

  public AbstractEconomyPlugin getPlugin() {
    return plugin;
  }

  public void load() {
    YamlConfiguration config = YamlConfiguration.loadConfiguration(localeFile);
    for (MessageType type : MessageType.values())
      if (config.contains(type.getKey()))
        messages.put(type, config.getString(type.getKey()));
  }

  public @NotNull String getMessage(@NotNull MessageType type) {
    if (!messages.containsKey(type))
      return plugin.getEconomyPlatform().getDefaultLocale().getMessage(type);
    return messages.get(type);
  }

}
