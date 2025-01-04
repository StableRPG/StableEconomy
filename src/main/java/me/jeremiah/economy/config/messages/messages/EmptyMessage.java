package me.jeremiah.economy.config.messages.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@DelegateDeserialization(AbstractMessage.class)
public final class EmptyMessage extends AbstractMessage<Void> {

  EmptyMessage() {
    super();
  }

  @Override
  @NotNull Void parse(TagResolver... resolvers) {
    return null;
  }

  @Override
  void send(@NotNull Audience targets, @NotNull Void message) {}

  @Override
  public void send(@NotNull Audience target, TagResolver... resolvers) {}

  @Override
  public void send(@NotNull Collection<? extends @NotNull Audience> targets, TagResolver... resolvers) {}

  @Override
  public void send() {}

  @Override
  public @NotNull Map<String, Object> serialize() {
    HashMap<String, Object> data = new HashMap<>();

    data.put("type", "empty");

    return data;
  }
}
