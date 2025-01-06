package me.jeremiah.economy.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.jeremiah.economy.EconomyPlatform;
import me.jeremiah.economy.currency.Currency;
import me.jeremiah.economy.data.PlayerAccount;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlaceholderAPIHook extends PlaceholderExpansion implements Closeable {

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
    return List.of(
      "leaderboard_#<position>_username", "leaderboard_#<position>_balance",
      "leaderboard_<currency>_#<position>_username", "leaderboard_<currency>_#<position>_balance",
      "balance", "balance_<currency>", "balance_<currency>_<uuid>", "balance_<currency>_<username>"
    );
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
    String[] args = params.split("_");
    if (args.length == 0)
      return null;
    return switch (args[0]) {
      case "leaderboard" -> {
        if (args.length < 3)
          yield null;

        Currency currency;
        int position;
        String display;
        if (args[1].startsWith("#")) {
          currency = platform.getCurrencyConfig().getDefaultCurrency();
          position = Integer.parseInt(args[1].substring(1)) - 1;
          display = args[2];
        } else {
          currency = platform.getCurrencyConfig().getCurrency(args[1]);
          position = Integer.parseInt(args[2].substring(1)) - 1;
          display = args[3];
        }

        PlayerAccount account = currency.getLeaderboardEntry(position);

        if (account == null)
          yield "N/A";

        if (display.equals("username"))
          yield account.getUsername();
        else if (display.equals("balance"))
          yield currency.getBalanceFormatted(account);

        yield null;
      }
      case "balance" -> {
        if (args.length == 1) {
          if (player == null)
            yield null;
          yield platform.getCurrencyConfig().getDefaultCurrency().getBalanceFormatted(player);
        } else if (args.length == 2) {
          Currency currency = platform.getCurrencyConfig().getCurrency(args[1]);
          if (currency != null)
            yield currency.getBalanceFormatted(player);
          else {
            PlayerAccount account;
            try {
              UUID target = UUID.fromString(args[1]);
              Optional<PlayerAccount> optional = platform.getDatabase().getByUUID(target);
              if (optional.isEmpty())
                yield null;
              account = optional.get();
            } catch (Exception ignored) {
              String username = args[1];
              Optional<PlayerAccount> optional = platform.getDatabase().getByUsername(username);
              if (optional.isEmpty())
                yield null;
              account = optional.get();
            }
            yield platform.getCurrencyConfig().getDefaultCurrency().getBalanceFormatted(account);
          }
        } else if (args.length == 3) {
          PlayerAccount account;
          try {
            UUID target = UUID.fromString(args[2]);
            Optional<PlayerAccount> optional = platform.getDatabase().getByUUID(target);
            if (optional.isEmpty())
              yield "No Account Found";
            account = optional.get();
          } catch (Exception ignored) {
            String username = args[2];
            Optional<PlayerAccount> optional = platform.getDatabase().getByUsername(username);
            if (optional.isEmpty())
              yield "No Account Found";
            account = optional.get();
          }
          yield platform.getCurrencyConfig().getCurrency(args[1]).getBalanceFormatted(account);
        }

        yield null;
      }
      default -> null;
    };
  }

  @Override
  public void close() {
    unregister();
  }

}
