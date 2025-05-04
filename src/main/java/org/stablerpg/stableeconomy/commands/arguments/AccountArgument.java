package org.stablerpg.stableeconomy.commands.arguments;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.stablerpg.stableeconomy.currency.Currency;
import org.stablerpg.stableeconomy.data.PlayerAccount;

public class AccountArgument extends CustomArgument<PlayerAccount, OfflinePlayer> {

  public AccountArgument(Currency currency) {
    this("target", currency);
  }

  public AccountArgument(String nodeName, Currency currency) {
    super(new OfflinePlayerArgument(nodeName), info -> {
      OfflinePlayer target = info.currentInput();

      if (target == null) throw CustomArgumentException.fromString("Player not found");

      PlayerAccount account = currency.getPlatform().getAccount(target);

      if (account == null)
        throw CustomArgumentException.fromString("Account not found for player: " + target.getName());

      return account;
    });
    replaceSuggestions(ArgumentSuggestions.strings(ignored -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)));
  }

}
