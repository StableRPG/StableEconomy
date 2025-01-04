package me.jeremiah.economy.config.messages;

import me.jeremiah.economy.AbstractEconomyPlugin;
import me.jeremiah.economy.config.AbstractConfig;
import me.jeremiah.economy.config.messages.messages.AbstractMessage;
import me.jeremiah.economy.config.messages.messages.Messages;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public final class MessagesConfig extends AbstractConfig implements Locale {

  private HashMap<MessageType, AbstractMessage<?>> messages;

  public MessagesConfig(@NotNull AbstractEconomyPlugin plugin) {
    super(plugin, "messages.yml");
  }

  public void load() {
    super.load();
    messages = new HashMap<>();
    for (MessageType type : MessageType.values()) {
      if (getConfig().isString(type.getKey())) {
        messages.put(type, Messages.chat(getConfig().getString(type.getKey())));
        continue;
      }
      ConfigurationSection section = getConfig().getConfigurationSection(type.getKey());
      if (section != null)
        messages.put(type, (AbstractMessage<?>) ConfigurationSerialization.deserializeObject(section.getValues(true), AbstractMessage.class));
    }
  }

  public @NotNull AbstractMessage<?> getMessage(@NotNull MessageType type) {
    return messages.get(type);
  }

}
