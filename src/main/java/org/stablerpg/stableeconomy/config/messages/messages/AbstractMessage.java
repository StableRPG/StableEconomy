package org.stablerpg.stableeconomy.config.messages.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractMessage<M> {

  private @NotNull Audience targets = Audience.empty();

  public AbstractMessage<M> toAll() {
    return targets(Bukkit.getOnlinePlayers());
  }

  public AbstractMessage<M> toAllExcept(@NotNull Audience @NotNull ... targets) {
    List<Audience> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
    onlinePlayers.removeAll(List.of(targets));
    return targets(onlinePlayers);
  }

  public AbstractMessage<M> targets(@NotNull Audience @NotNull ... targets) {
    return targets(List.of(targets));
  }

  public AbstractMessage<M> targets(@NotNull Collection<? extends @NotNull Audience> targets) {
    this.targets = Audience.audience(targets);
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
