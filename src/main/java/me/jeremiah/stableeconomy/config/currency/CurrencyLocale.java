package me.jeremiah.stableeconomy.config.currency;

import me.jeremiah.stableeconomy.EconomyPlatform;
import me.jeremiah.stableeconomy.config.messages.Locale;
import me.jeremiah.stableeconomy.config.messages.MessageType;
import me.jeremiah.stableeconomy.config.messages.messages.AbstractMessage;
import me.jeremiah.stableeconomy.config.messages.messages.Messages;
import org.bukkit.configuration.ConfigurationSection;
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
  private final HashMap<MessageType, AbstractMessage<?>> messages = new HashMap<>();

  private CurrencyLocale(EconomyPlatform platform, File localeFile) {
    this.platform = platform;
    this.localeFile = localeFile;
  }

  public void load() {
    YamlConfiguration config = YamlConfiguration.loadConfiguration(localeFile);
    for (MessageType type : MessageType.values()) {
      if (config.isString(type.getKey())) {
        messages.put(type, Messages.chat(config.getString(type.getKey())));
        continue;
      }
      ConfigurationSection section = config.getConfigurationSection(type.getKey());
      if (section != null)
        messages.put(type, Messages.fromYaml(section));
    }
  }

  public @NotNull AbstractMessage<?> getMessage(@NotNull MessageType type) {
    if (!messages.containsKey(type))
      return platform.getDefaultLocale().getMessage(type);
    return messages.get(type);
  }

}
