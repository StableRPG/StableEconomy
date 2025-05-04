package org.stablerpg.stableeconomy.config.currency;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.config.messages.Locale;
import org.stablerpg.stableeconomy.config.messages.MessageType;
import org.stablerpg.stableeconomy.config.messages.messages.AbstractMessage;
import org.stablerpg.stableeconomy.config.messages.messages.Messages;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class CurrencyLocale implements Locale {

  private final EconomyPlatform platform;
  private final File localeFile;

  private Map<MessageType, AbstractMessage<?>> messages;

  public CurrencyLocale(EconomyPlatform platform, File localeFile) {
    this.platform = platform;
    this.localeFile = localeFile;
  }

  public void load() {
    YamlConfiguration config = YamlConfiguration.loadConfiguration(localeFile);

    messages = new HashMap<>();
    for (MessageType type : MessageType.values()) {
      if (config.isString(type.getKey())) {
        messages.put(type, Messages.chat(config.getString(type.getKey())));
        continue;
      }
      ConfigurationSection section = config.getConfigurationSection(type.getKey());
      if (section != null) messages.put(type, Messages.fromYaml(section));
    }
  }

  @Override
  public @NotNull Logger getLogger() {
    return platform.getLogger();
  }

  public @NotNull AbstractMessage<?> getMessage(@NotNull MessageType type) {
    if (!messages.containsKey(type))
      return platform.getDefaultLocale().getMessage(type);
    return messages.get(type);
  }

}
