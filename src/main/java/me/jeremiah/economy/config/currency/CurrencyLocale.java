package me.jeremiah.economy.config.currency;

import me.jeremiah.economy.EconomyPlatform;
import me.jeremiah.economy.config.messages.Locale;
import me.jeremiah.economy.config.messages.MessageType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;

public final class CurrencyLocale implements Locale {

  public static CurrencyLocale of(EconomyPlatform plugin, File localeFile) {
    return new CurrencyLocale(plugin, localeFile);
  }

  private final EconomyPlatform platform;

  private final File localeFile;
  private final HashMap<MessageType, String> messages = new HashMap<>();

  private CurrencyLocale(EconomyPlatform platform, File localeFile) {
    this.platform = platform;
    this.localeFile = localeFile;
  }

  public void load() {
    YamlConfiguration config = YamlConfiguration.loadConfiguration(localeFile);
    for (MessageType type : MessageType.values())
      if (config.contains(type.getKey()))
        messages.put(type, config.getString(type.getKey()));
  }

  public @NotNull String getMessage(@NotNull MessageType type) {
    if (!messages.containsKey(type))
      return platform.getDefaultLocale().getMessage(type);
    return messages.get(type);
  }

}
