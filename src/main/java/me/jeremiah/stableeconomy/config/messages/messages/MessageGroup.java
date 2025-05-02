package me.jeremiah.stableeconomy.config.messages.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public final class MessageGroup extends AbstractMessage<Void> {

  private final List<? extends AbstractMessage<?>> messages;

  MessageGroup(List<? extends AbstractMessage<?>> messages) {
    this.messages = messages;
  }

  public AbstractMessage<Void> targets(@NotNull Collection<? extends @NotNull Audience> targets) {
    messages.forEach(message -> message.targets(targets));
    return this;
  }

  @Override
  @NotNull
  Void parse(TagResolver... resolvers) {
    return null;
  }

  @Override
  void send(@NotNull Audience target, @NotNull Void messages) {
    this.messages.forEach(message -> message.send(target));
  }

  @Override
  public void send(@NotNull Audience target, TagResolver... resolvers) {
    messages.forEach(message -> message.send(target, resolvers));
  }

  @Override
  public void send(@NotNull Collection<? extends @NotNull Audience> targets, TagResolver... resolvers) {
    messages.forEach(message -> message.send(targets, resolvers));
  }

  @Override
  public void send() {
    messages.forEach(AbstractMessage::send);
  }

}
