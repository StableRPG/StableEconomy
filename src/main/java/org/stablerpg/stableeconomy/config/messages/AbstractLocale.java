package org.stablerpg.stableeconomy.config.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.config.messages.messages.AbstractMessage;

import java.util.Map;

public abstract class AbstractLocale<T extends Enum<T>> {

  private final Map<T, AbstractMessage<?>> messages;

  protected AbstractLocale(Map<T, AbstractMessage<?>> messages) {
    this.messages = messages;
  }

  public @NotNull AbstractMessage<?> getMessage(@NotNull T type) {
    return messages.get(type);
  }

  public void sendParsedMessage(@NotNull Audience audience, @NotNull T id, @Subst("") @NotNull String... tags) {
    TagResolver[] tagResolvers = new TagResolver[tags.length / 2];
    for (int i = 0; i < tags.length; i++)
      tagResolvers[i / 2] = TagResolver.resolver(tags[i], Tag.selfClosingInserting(MiniMessage.miniMessage().deserialize(tags[++i])));

    AbstractMessage<?> message = getMessage(id);

    message.send(audience, tagResolvers);
  }

  public void sendParsedMessage(@NotNull Audience audience, @NotNull String message) {
    audience.sendMessage(MiniMessage.miniMessage().deserialize(message));
  }

}
