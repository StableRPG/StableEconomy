package me.jeremiah.economy.config.messages;

import me.jeremiah.economy.AbstractEconomyPlugin;
import me.jeremiah.economy.config.AbstractConfig;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public final class MessagesConfig extends AbstractConfig implements Locale {

  private HashMap<MessageType, String> messages;

  public MessagesConfig(@NotNull AbstractEconomyPlugin plugin) {
    super(plugin, "messages.yml");
  }

  public void load() {
    super.load();
    messages = new HashMap<>();
    for (MessageType type : MessageType.values())
      messages.put(type, getConfig().getString(type.getKey(), ""));
  }

  public @NotNull String getMessage(@NotNull MessageType type) {
    return messages.get(type);
  }

}
