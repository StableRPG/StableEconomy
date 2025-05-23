package org.stablerpg.stableeconomy.config.shop;

import net.kyori.adventure.audience.Audience;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;
import org.stablerpg.stableeconomy.config.messages.AbstractLocale;
import org.stablerpg.stableeconomy.config.messages.messages.AbstractMessage;
import org.stablerpg.stableeconomy.config.messages.messages.Messages;

import java.util.HashMap;
import java.util.Map;

public class ShopLocale extends AbstractLocale<ShopMessageType> {

  public static ShopLocale deserialize(ConfigurationSection section) throws DeserializationException {
    Map<ShopMessageType, AbstractMessage<?>> messages = new HashMap<>();
    for (ShopMessageType type : ShopMessageType.values()) {
      if (section.isString(type.getKey())) {
        messages.put(type, Messages.chat(section.getString(type.getKey())));
        continue;
      }

      ConfigurationSection messageSection = section.getConfigurationSection(type.getKey());

      if (messageSection == null)
        throw new DeserializationException("Missing message for " + type.getKey());

      messages.put(type, Messages.deserialize(messageSection));
    }

    return new ShopLocale(messages);
  }

  private ShopLocale(Map<ShopMessageType, AbstractMessage<?>> messages) {
    super(messages);
  }

  @Override
  public void sendParsedMessage(@NotNull Audience audience, @NotNull ShopMessageType id, @NotNull String... tags) {
    id.checkTags(tags);
    super.sendParsedMessage(audience, id, tags);
  }

}
