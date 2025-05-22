package org.stablerpg.stableeconomy.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.currency.Currency;
import org.stablerpg.stableeconomy.data.PlayerAccount;

import java.io.Closeable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public final class PlaceholderAPIHook extends PlaceholderExpansion implements Closeable {

  private final EconomyPlatform platform;

  public PlaceholderAPIHook(EconomyPlatform platform) {
    this.platform = platform;
    register();
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
    return List.of("leaderboard_#<position>_username", "leaderboard_#<position>_balance", "leaderboard_<currency>_#<position>_username", "leaderboard_<currency>_#<position>_balance", "balance", "balance_<currency>", "balance_<currency>_<uuid>", "balance_<currency>_<username>");
  }

  public boolean persist() {
    return true;
  }

  @Override
  public void close() {
    unregister();
  }  @Override
  public @Nullable String onPlaceholderRequest(final Player player, @NotNull final String params) {
    return onRequest(player, params);
  }

  @Override
  public @Nullable String onRequest(final OfflinePlayer player, @NotNull final String params) {
    String[] args = params.split("_");
    if (args.length == 0) return null;
    return switch (args[0]) {
      case "leaderboard" -> {
        if (args.length < 3) yield null;

        Currency currency;
        int position;
        String display;
        if (args[1].startsWith("#")) {
          currency = platform.getCurrencyHolder().getDefaultCurrency();
          position = Integer.parseInt(args[1].substring(1)) - 1;
          display = args[2];
        } else {
          Optional<Currency> optionalCurrency = platform.getCurrencyHolder().getCurrency(args[1]);
          if (optionalCurrency.isEmpty()) yield null;
          currency = optionalCurrency.get();
          position = Integer.parseInt(args[2].substring(1)) - 1;
          display = args[3];
        }

        PlayerAccount account = currency.getLeaderboardEntry(position);

        if (account == null) if (display.equals("username")) yield "N/A";
        else if (display.equals("balance")) yield currency.format(0);

        if (display.equals("username")) yield account.getUsername();
        else if (display.equals("balance")) yield currency.getFormattedBalance(account);

        yield null;
      }
      case "balance" -> {
        if (args.length == 1) {
          if (player == null) yield null;
          yield platform.getCurrencyHolder().getDefaultCurrency().getFormattedBalance(player);
        } else if (args.length == 2) {
          Optional<Currency> optionalCurrency = platform.getCurrencyHolder().getCurrency(args[1]);
          if (optionalCurrency.isPresent()) yield optionalCurrency.get().getFormattedBalance(player);
          else {
            PlayerAccount account;
            try {
              UUID target = UUID.fromString(args[1]);
              account = platform.getAccount(target);
              if (account == null) yield "No Account Found";
            } catch (Exception ignored) {
              String username = args[1];
              account = platform.getAccount(username);
              if (account == null) yield "No Account Found";
            }
            yield platform.getCurrencyHolder().getDefaultCurrency().getFormattedBalance(account);
          }
        } else if (args.length == 3) {
          PlayerAccount account;
          try {
            UUID target = UUID.fromString(args[2]);
            account = platform.getAccount(target);
            if (account == null) yield "No Account Found";
          } catch (Exception ignored) {
            String username = args[2];
            account = platform.getAccount(username);
            if (account == null) yield "No Account Found";
          }
          Optional<Currency> optionalCurrency = platform.getCurrencyHolder().getCurrency(args[1]);
          if (optionalCurrency.isEmpty()) yield null;
          yield optionalCurrency.get().getFormattedBalance(account);
        }

        yield null;
      }
      default -> null;
    };
  }



}
