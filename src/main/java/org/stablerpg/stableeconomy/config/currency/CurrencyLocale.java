package org.stablerpg.stableeconomy.config.currency;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;
import org.stablerpg.stableeconomy.config.messages.Locale;
import org.stablerpg.stableeconomy.config.messages.MessageType;
import org.stablerpg.stableeconomy.config.messages.messages.AbstractMessage;
import org.stablerpg.stableeconomy.config.messages.messages.Messages;

import java.util.HashMap;
import java.util.Map;

public final class CurrencyLocale implements Locale {

  public static CurrencyLocale deserialize(ConfigurationSection section) throws DeserializationException {
    Map<MessageType, AbstractMessage<?>> messages = new HashMap<>();
    for (MessageType type : MessageType.values()) {
      if (section.isString(type.getKey())) {
        messages.put(type, Messages.chat(section.getString(type.getKey())));
        continue;
      }
      ConfigurationSection messageSection = section.getConfigurationSection(type.getKey());
      if (messageSection != null)
        messages.put(type, Messages.deserialize(messageSection));
      else
        throw new DeserializationException("Missing message for " + type.getKey());
    }

    return new CurrencyLocale(messages);
  }

  private final Map<MessageType, AbstractMessage<?>> messages;

  private CurrencyLocale(Map<MessageType, AbstractMessage<?>> messages) {
    this.messages = messages;
  }

  public @NotNull AbstractMessage<?> getMessage(@NotNull MessageType type) {
    return messages.get(type);
  }

}
