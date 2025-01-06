package me.jeremiah.economy.command;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import me.jeremiah.economy.currency.Currency;
import me.jeremiah.economy.data.PlayerAccount;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class AccountArgument extends CustomArgument<PlayerAccount, OfflinePlayer> {

  public AccountArgument(Currency currency) {
    this("target", currency);
  }

  public AccountArgument(String nodeName, Currency currency) {
    super(
      new OfflinePlayerArgument(nodeName),
      info -> {
        OfflinePlayer target = info.currentInput();

        if (target == null)
          throw CustomArgument.CustomArgumentException.fromString("Player not found");

        return currency.getPlatform().getDatabase().getByPlayer(target)
          .orElseThrow(() -> CustomArgument.CustomArgumentException.fromString("Account not found"));
      }
    );
    replaceSuggestions(ArgumentSuggestions.strings(ignored -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)));
  }

}
