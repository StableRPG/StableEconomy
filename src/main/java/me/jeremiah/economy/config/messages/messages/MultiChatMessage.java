package me.jeremiah.economy.config.messages.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DelegateDeserialization(AbstractMessage.class)
public final class MultiChatMessage extends AbstractMessage<List<Component>> {

  private final @NotNull List<@NotNull String> messages;

  MultiChatMessage(String... messages) {
    this.messages = Arrays.asList(messages);
  }

  MultiChatMessage(@NotNull List<@NotNull String> messages) {
    this.messages = List.copyOf(messages);
  }

  @Override
  @NotNull
  List<Component> parse(TagResolver... resolvers) {
    return messages.stream()
        .map(message -> MiniMessage.miniMessage().deserialize(message, resolvers))
        .toList();
  }

  @Override
  void send(@NotNull Audience target, @NotNull List<Component> messages) {
    messages.forEach(target::sendMessage);
  }

  @Override
  public @NotNull Map<String, Object> serialize() {
    Map<String, Object> data = new HashMap<>();

    data.put("type", "chat");
    if (this.messages.size() == 1)
      data.put("message", this.messages.getFirst());
    else
      data.put("messages", this.messages);

    return data;
  }

}
