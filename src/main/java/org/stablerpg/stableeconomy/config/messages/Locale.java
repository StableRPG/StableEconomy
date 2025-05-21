package org.stablerpg.stableeconomy.config.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.config.messages.messages.AbstractMessage;

public interface Locale {

  default void sendParsedMessage(@NotNull Audience audience, @NotNull MessageType id, @Subst("") @NotNull String... tags) {
    id.checkTags(tags);

    TagResolver[] tagResolvers = new TagResolver[tags.length / 2];
    for (int i = 0; i < tags.length; i++)
      tagResolvers[i / 2] = TagResolver.resolver(tags[i], Tag.selfClosingInserting(Component.text(tags[++i])));

    AbstractMessage<?> message = getMessage(id);

    message.send(audience, tagResolvers);
  }

  @NotNull AbstractMessage<?> getMessage(@NotNull MessageType type);

  default void sendParsedMessage(@NotNull Audience audience, @NotNull String message) {
    audience.sendMessage(MiniMessage.miniMessage().deserialize(message));
  }

}
