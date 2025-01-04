package me.jeremiah.economy.config.messages.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@DelegateDeserialization(AbstractMessage.class)
public final class ActionBarMessage extends AbstractMessage<Component> {

  private @NotNull String message;

  ActionBarMessage(@NotNull String message) {
    this.message = message;
  }

  public void message(@NotNull String message) {
    this.message = message;
  }

  public @NotNull String message() {
    return this.message;
  }

  @Override
  @NotNull
  Component parse(TagResolver... resolvers) {
    return MiniMessage.miniMessage().deserialize(this.message, resolvers);
  }

  @Override
  void send(@NotNull Audience target, @NotNull Component message) {
    target.sendActionBar(message);
  }

  @Override
  public @NotNull Map<String, Object> serialize() {
    Map<String, Object> data = new HashMap<>();

    data.put("type", "actionbar");
    data.put("message", this.message);

    return data;
  }
}
