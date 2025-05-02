package org.stablerpg.stableeconomy.config.messages;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.AbstractEconomyPlugin;
import org.stablerpg.stableeconomy.config.AbstractConfig;
import org.stablerpg.stableeconomy.config.messages.messages.AbstractMessage;
import org.stablerpg.stableeconomy.config.messages.messages.Messages;

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
        messages.put(type, Messages.fromYaml(section));
    }
  }

  public @NotNull AbstractMessage<?> getMessage(@NotNull MessageType type) {
    return messages.get(type);
  }

}
