package me.jeremiah.economy.config.messages.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SerializableAs("AbstractMessage")
public abstract class AbstractMessage<M> implements ConfigurationSerializable {

  public static @NotNull AbstractMessage<?> deserialize(@NotNull Map<String, Object> args) {
    String messageType = (String) args.get("type");

    if (messageType == null)
      throw new IllegalArgumentException("Message type is not defined");

    return switch (messageType.toLowerCase()) {
      case "group" -> {
        List<?> rawMessages = (List<?>) args.get("messages");

        if (rawMessages == null)
          throw new IllegalArgumentException("Messages are not defined");

        List<? extends AbstractMessage<?>> messages = rawMessages.stream()
          .map(key -> (Map<String, Object>) key)
          .map(AbstractMessage::deserialize)
          .toList();

        yield Messages.group(messages);
      }
      case "chat" -> {
        if (args.containsKey("message")) {
          String message = (String) args.get("message");
          yield Messages.chat(message);
        }
        List<?> rawMessages = (List<?>) args.get("messages");

        if (rawMessages == null)
          throw new IllegalArgumentException("Messages are not defined");

        List<String> messages = rawMessages.stream().map(String::valueOf).toList();

        yield Messages.chat(messages);
      }
      case "actionbar" -> {
        String message = (String) args.get("message");

        if (message == null)
          throw new IllegalArgumentException("Message is not defined");

        yield Messages.actionBar(message);
      }
      case "title" -> {
        String title = (String) args.get("title");
        String subtitle = (String) args.get("subtitle");
        long fadeIn = (int) args.getOrDefault("fade-in", 10);
        long stay = (int) args.getOrDefault("stay", 70);
        long fadeOut = (int) args.getOrDefault("fade-out", 20);

        if (title == null)
          throw new IllegalArgumentException("Title is not defined");

        if (subtitle == null)
          throw new IllegalArgumentException("Subtitle is not defined");

        Title.Times times = Title.Times.times(
          Ticks.duration(fadeIn),
          Ticks.duration(stay),
          Ticks.duration(fadeOut)
        );

        yield Messages.title(title, subtitle, times);
      }
      case "sound" -> {
        String sound = (String) args.get("sound");
        float volume = (float) args.getOrDefault("volume", 1.0);
        float pitch = (float) args.getOrDefault("pitch", 1.0);

        if (sound == null)
          throw new IllegalArgumentException("Sound is not defined");

        yield Messages.sound(sound, volume, pitch);
      }
      case "empty" -> Messages.empty();
      default -> throw new IllegalArgumentException("Unknown message type: " + messageType);
    };
  }

  private @NotNull Collection<? extends @NotNull Audience> targets = List.of();

  public AbstractMessage<M> toAll() {
    return targets(Bukkit.getOnlinePlayers());
  }

  public AbstractMessage<M> toAllExcept(@NotNull Audience @NotNull... targets) {
    List<Audience> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
    onlinePlayers.removeAll(List.of(targets));
    return targets(onlinePlayers);
  }

  public AbstractMessage<M> targets(@NotNull Audience @NotNull... targets) {
    return targets(List.of(targets));
  }

  public AbstractMessage<M> targets(@NotNull Collection<? extends @NotNull Audience> targets) {
    this.targets = List.copyOf(targets);
    return this;
  }

  abstract @NotNull M parse(TagResolver... resolvers);

  abstract void send(@NotNull Audience targets, @NotNull M message);

  public void send(@NotNull Audience target, TagResolver... resolvers) {
    send(target, parse(resolvers));
  }

  public void send(@NotNull Collection<? extends @NotNull Audience> targets, TagResolver... resolvers) {
      M message = parse(resolvers);
      targets.forEach(target -> send(target, message));
  }

  public void send() {
    send(targets);
  }

}
