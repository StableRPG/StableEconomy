package me.jeremiah.stableeconomy.config.messages.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

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

}
