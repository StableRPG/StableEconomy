package me.jeremiah.economy.config;

import me.jeremiah.economy.AbstractEconomyPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface Locale {

  AbstractEconomyPlugin getPlugin();

  void load();

  @NotNull String getMessage(@NotNull MessageType type);

  default void sendParsedMessage(@NotNull CommandSender sender, @NotNull MessageType id, @NotNull String... tags) {
    sender.sendMessage(getParsedMessage(id, tags));
  }

  default void sendParsedMessage(@NotNull CommandSender sender, @NotNull String message, @NotNull String... tags) {
    sender.sendMessage(getParsedMessage(message, tags));
  }

  default Component getParsedMessage(@NotNull MessageType id, @NotNull String... tags) {
    id.checkTags(tags);
    return getParsedMessage(getMessage(id), tags);
  }

  private Component getParsedMessage(String message, String... tags) {
    if (tags.length == 0)
      return MiniMessage.miniMessage().deserialize(message);
    if (tags.length % 2 != 0)
      throw new IllegalArgumentException("Invalid number of tags, found odd length when even is required");
    TagResolver[] tagResolvers = new TagResolver[tags.length / 2];
    for (int i = 0; i < tags.length; i++)
      tagResolvers[i / 2] = TagResolver.resolver(tags[i], Tag.selfClosingInserting(Component.text(tags[++i])));
    return MiniMessage.miniMessage().deserialize(message, tagResolvers);
  }

}
