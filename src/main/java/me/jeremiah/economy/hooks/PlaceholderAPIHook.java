package me.jeremiah.economy.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.jeremiah.economy.EconomyPlatform;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;

public class PlaceholderAPIHook extends PlaceholderExpansion implements Closeable {

  private final EconomyPlatform platform;

  public PlaceholderAPIHook(EconomyPlatform platform) {
    this.platform = platform;
  }

  public @NotNull String getIdentifier() {
    return platform.getPlugin().getPluginMeta().getName().toLowerCase();
  }

  public @NotNull String getAuthor() {
    return String.join(", ", platform.getPlugin().getPluginMeta().getAuthors());
  }

  public @NotNull String getVersion() {
    return platform.getPlugin().getPluginMeta().getVersion();
  }

  public @NotNull String getName() {
    return platform.getPlugin().getPluginMeta().getName();
  }

  public @NotNull List<String> getPlaceholders() {
    return Collections.emptyList();
  }

  public boolean persist() {
    return true;
  }


  @Override
  public @Nullable String onPlaceholderRequest(final Player player, @NotNull final String params) {
    return onRequest(player, params);
  }

  @Override
  public @Nullable String onRequest(final OfflinePlayer player, @NotNull final String params) {
    return null;
  }


  @Override
  public void close() {

  }

}
