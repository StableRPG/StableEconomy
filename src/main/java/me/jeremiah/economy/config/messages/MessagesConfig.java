package me.jeremiah.economy.config.messages;

import me.jeremiah.economy.AbstractEconomyPlugin;
import me.jeremiah.economy.config.AbstractConfig;
import me.jeremiah.economy.config.messages.messages.AbstractMessage;
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
    for (MessageType type : MessageType.values())
      messages.put(type, getConfig().getSerializable(type.getKey(), AbstractMessage.class));
  }

  public @NotNull AbstractMessage<?> getMessage(@NotNull MessageType type) {
    return messages.get(type);
  }

}
