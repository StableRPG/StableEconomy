package me.jeremiah.stableeconomy.config.messages.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public final class ChatMessage extends AbstractMessage<List<Component>> {

  private final @NotNull List<@NotNull String> messages;

  ChatMessage(String... messages) {
    this.messages = Arrays.asList(messages);
  }

  ChatMessage(@NotNull List<@NotNull String> messages) {
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

}
