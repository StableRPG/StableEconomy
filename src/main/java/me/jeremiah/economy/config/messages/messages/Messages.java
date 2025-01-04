package me.jeremiah.economy.config.messages.messages;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;

import java.util.Arrays;
import java.util.List;

public final class Messages {

  private Messages() {
    throw new UnsupportedOperationException();
  }

  private static final EmptyMessage EMPTY_MESSAGE = new EmptyMessage();

  public static EmptyMessage empty() {
    return EMPTY_MESSAGE;
  }

  public static MessageGroup group(AbstractMessage<?>... messages) {
    return group(List.of(messages));
  }

  public static MessageGroup group(List<? extends AbstractMessage<?>> messages) {
    return new MessageGroup(messages);
  }

  public static ChatMessage chat(Component... messages) {
    String[] serialized = Arrays.stream(messages).map(MiniMessage.miniMessage()::serialize).toArray(String[]::new);
    return chat(serialized);
  }

  public static ChatMessage chat(String... messages) {
    return new ChatMessage(messages);
  }

  public static ChatMessage chat(List<String> messages) {
    return new ChatMessage(messages);
  }

  public static ActionBarMessage actionBar(Component message) {
    return new ActionBarMessage(MiniMessage.miniMessage().serialize(message));
  }

  public static ActionBarMessage actionBar(String message) {
    return new ActionBarMessage(message);
  }

  public static TitleMessage title(Title title) {
    return title(title.title(), title.subtitle(), title.times());
  }

  public static TitleMessage title(Component title) {
    return title(MiniMessage.miniMessage().serialize(title));
  }

  public static TitleMessage title(Component title, Component subtitle) {
    return title(MiniMessage.miniMessage().serialize(title), MiniMessage.miniMessage().serialize(subtitle));
  }

  public static TitleMessage title(Component title, Component subtitle, Title.Times times) {
    return title(MiniMessage.miniMessage().serialize(title), MiniMessage.miniMessage().serialize(subtitle), times);
  }

  public static TitleMessage title(String title) {
    return title(title, "");
  }

  public static TitleMessage title(String title, String subtitle) {
    return title(title, subtitle, Title.DEFAULT_TIMES);
  }

  public static TitleMessage title(String title, String subtitle, Title.Times times) {
    return new TitleMessage(title, subtitle, times);
  }

  public static SoundMessage sound(String sound) {
    return sound(sound, Sound.Source.MASTER);
  }

  public static SoundMessage sound(String sound, Sound.Source source) {
    return sound(sound, source, 1.0f, 1.0f);
  }

  public static SoundMessage sound(String sound, float volume, float pitch) {
    return sound(sound, Sound.Source.MASTER, volume, pitch);
  }

  public static SoundMessage sound(String sound, Sound.Source source, float volume, float pitch) {
    return sound(Sound.sound(Key.key(sound), source, volume, pitch));
  }

  public static SoundMessage sound(Sound sound) {
    return new SoundMessage(sound.name(), sound.source(), sound.volume(), sound.pitch());
  }

}
