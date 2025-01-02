package me.jeremiah.economy.currency;

import com.google.common.base.Preconditions;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.jeremiah.economy.EconomyPlatform;
import me.jeremiah.economy.config.messages.Locale;
import me.jeremiah.economy.config.messages.MessageType;
import me.jeremiah.economy.data.PlayerAccount;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class Currency {

  private final @NotNull String name;
  private final @NotNull EconomyPlatform platform;
  private final @NotNull Locale locale;

  private final @Nullable CommandTree viewCommand;

  private final @Nullable CommandTree transferCommand;

  private final @Nullable CommandTree leaderboardCommand;
  private final @Nullable ArrayList<PlayerAccount> leaderboard;
  private @Nullable ScheduledTask autoUpdateLeaderboard;

  private final @Nullable CommandTree adminCommand;

  private Currency(@NotNull String name, @NotNull EconomyPlatform platform, @Nullable Locale locale,
                   @Nullable CommandTree viewCommand,
                   @Nullable CommandTree transferCommand,
                   @Nullable CommandTree leaderboardCommand, @Nullable ArrayList<PlayerAccount> leaderboard,
                   @Nullable CommandTree adminCommand) {
    this.name = name;
    this.platform = platform;
    this.locale = locale != null ? locale : platform.getDefaultLocale();
    this.viewCommand = viewCommand;
    this.transferCommand = transferCommand;
    this.leaderboardCommand = leaderboardCommand;
    this.leaderboard = leaderboard;
    this.adminCommand = adminCommand;
    register();
  }

  public @NotNull String getName() {
    return name;
  }

  public boolean isDefaultCurrency() {
    return name.equals("default");
  }

  public @NotNull EconomyPlatform getPlatform() {
    return platform;
  }

  public @NotNull Locale getLocale() {
    return locale;
  }

  public void register() {
    if (viewCommand != null) viewCommand.register();
    if (transferCommand != null) transferCommand.register();
    if (leaderboardCommand != null) leaderboardCommand.register();
    if (leaderboard != null) this.autoUpdateLeaderboard = Bukkit.getAsyncScheduler().runAtFixedRate(platform.getPlugin(), task -> {
      leaderboard.clear();
      leaderboard.addAll(platform.getDatabase().getEntries());
      leaderboard.sort(Comparator.comparing(account -> account.getBalanceEntry(name)));
    }, 0, 5, TimeUnit.MINUTES);
    if (adminCommand != null) adminCommand.register();
  }

  public void unregister() {
    if (viewCommand != null) CommandAPI.unregister(viewCommand.getName());
    if (transferCommand != null) CommandAPI.unregister(transferCommand.getName());
    if (leaderboardCommand != null) CommandAPI.unregister(leaderboardCommand.getName());
    if (leaderboard != null) leaderboard.clear();
    if (autoUpdateLeaderboard != null) {
      autoUpdateLeaderboard.cancel();
      autoUpdateLeaderboard = null;
    }
    if (adminCommand != null) CommandAPI.unregister(adminCommand.getName());
  }

  public static class Builder {

    private final String currency;
    private final EconomyPlatform platform;

    private Locale locale;

    private String viewCommandName;
    private String[] viewCommandAliases = new String[0];
    private String viewCommandPermission;

    private String transferCommandName;
    private String[] transferCommandAliases = new String[0];
    private String transferCommandPermission;

    private String leaderboardCommandName;
    private String[] leaderboardCommandAliases = new String[0];
    private String leaderboardCommandPermission;
    private ArrayList<PlayerAccount> leaderboard;

    private String adminCommandName;
    private String[] adminCommandAliases = new String[0];
    private String adminCommandPermission;

    public Builder(@NotNull String currency, @NotNull EconomyPlatform platform) {
      Preconditions.checkNotNull(currency, "Currency name cannot be null");
      this.currency = currency;
      this.platform = platform;
      this.locale = platform.getDefaultLocale();
    }

    public Builder usingYaml(@NotNull YamlConfiguration config) {
      // TODO: Load currency settings from YAML
      return this;
    }

    public Builder withLocale(@NotNull Locale locale) {
      this.locale = locale;
      return this;
    }

    public Builder withViewCommandName(String name) {
      this.viewCommandName = name;
      return this;
    }

    public Builder withViewCommandAliases(String... aliases) {
      this.viewCommandAliases = aliases;
      return this;
    }

    public Builder withViewCommandPermission(String permission) {
      this.viewCommandPermission = permission;
      return this;
    }

    public Builder withTransferCommandName(String name) {
      this.transferCommandName = name;
      return this;
    }

    public Builder withTransferCommandAliases(String... aliases) {
      this.transferCommandAliases = aliases;
      return this;
    }

    public Builder withTransferCommandPermission(String permission) {
      this.transferCommandPermission = permission;
      return this;
    }

    public Builder withLeaderboardCommandName(String name) {
      this.leaderboardCommandName = name;
      return this;
    }

    public Builder withLeaderboardCommandAliases(String... aliases) {
      this.leaderboardCommandAliases = aliases;
      return this;
    }

    public Builder withLeaderboardCommandPermission(String permission) {
      this.leaderboardCommandPermission = permission;
      return this;
    }

    public Builder withAdminCommandName(String name) {
      this.adminCommandName = name;
      return this;
    }

    public Builder withAdminCommandAliases(String... aliases) {
      this.adminCommandAliases = aliases;
      return this;
    }

    public Builder withAdminCommandPermission(String permission) {
      this.adminCommandPermission = permission;
      return this;
    }

    public Currency build() {
      CommandTree viewCommand = null;
      if (viewCommandName != null) {
        viewCommand = new CommandTree(viewCommandName).withAliases(viewCommandAliases);
        if (viewCommandPermission != null) viewCommand.withPermission(viewCommandPermission);
        viewCommand
          .then(new OfflinePlayerArgument("target")
            .replaceSuggestions(ArgumentSuggestions.strings(Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)))
            .executes(this::viewOtherBalance))
          .executesPlayer(this::viewOwnBalance);
      }

      CommandTree transferCommand = null;
      if (transferCommandName != null) {
        transferCommand = new CommandTree(transferCommandName).withAliases(transferCommandAliases);
        if (transferCommandPermission != null) transferCommand.withPermission(transferCommandPermission);
        transferCommand
          .then(new OfflinePlayerArgument("target")
            .replaceSuggestions(ArgumentSuggestions.strings(Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)))
            .then(new DoubleArgument("amount")
              .executesPlayer(this::transferBalance)));
      }

      CommandTree leaderboardCommand = null;
      if (leaderboardCommandName != null) {
        leaderboardCommand = new CommandTree(leaderboardCommandName).withAliases(leaderboardCommandAliases);
        leaderboard = new ArrayList<>();
        if (leaderboardCommandPermission != null) {
          leaderboardCommand.withPermission(leaderboardCommandPermission);
          leaderboardCommand
            .then(LiteralArgument.of("update")
              .withPermission(leaderboardCommandPermission + ".update")
              .executes(this::updateLeaderboard));
        }
        leaderboardCommand
          .then(new IntegerArgument("page", 1)
            .setOptional(true)
            .executes(this::viewLeaderboard));
      }

      CommandTree adminCommand = null;
      if (adminCommandName != null) {
        adminCommand = new CommandTree(adminCommandName).withAliases(adminCommandAliases);
        if (adminCommandPermission != null) adminCommand.withPermission(adminCommandPermission);
        adminCommand
          .then(LiteralArgument.of("give")
            .then(new OfflinePlayerArgument("target")
              .replaceSuggestions(ArgumentSuggestions.strings(Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)))
              .then(new DoubleArgument("amount")
                .executes(this::addBalance))))
          .then(LiteralArgument.of("take")
            .then(new OfflinePlayerArgument("target")
              .replaceSuggestions(ArgumentSuggestions.strings(Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)))
              .then(new DoubleArgument("amount")
                .executes(this::removeBalance))))
          .then(LiteralArgument.of("set")
            .then(new OfflinePlayerArgument("target")
              .replaceSuggestions(ArgumentSuggestions.strings(Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)))
              .then(new DoubleArgument("amount")
                .executes(this::setBalance))))
          .then(LiteralArgument.of("reset")
            .then(new OfflinePlayerArgument("target")
              .replaceSuggestions(ArgumentSuggestions.strings(Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)))
              .executes(this::resetBalance)));
      }

      return new Currency(currency, platform, locale, viewCommand, transferCommand, leaderboardCommand, leaderboard, adminCommand);
    }

    private void viewOtherBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
      OfflinePlayer target = (OfflinePlayer) args.get("target");

      if (target == null)
        throw CommandAPI.failWithString("Player not found");

      platform.getDatabase().getByPlayer(target).ifPresent(account -> locale.sendParsedMessage(sender, MessageType.VIEW_OTHER,
        "player", account.getUsername(),
        "balance", String.valueOf(account.getBalance(currency))));
    }

    private void viewOwnBalance(Player player, CommandArguments args) {
      platform.getDatabase().getByPlayer(player).ifPresent(account -> locale.sendParsedMessage(player, MessageType.VIEW_OWN,
        "balance", String.valueOf(account.getBalance(currency))));
    }

    private void transferBalance(Player player, CommandArguments args) throws WrapperCommandSyntaxException {
      OfflinePlayer target = (OfflinePlayer) args.get("target");
      Double amount = (Double) args.get("amount");

      if (target == null)
        throw CommandAPI.failWithString("Player not found");
      if (amount == null)
        throw CommandAPI.failWithString("Invalid amount");

      platform.getDatabase().getByPlayer(player).ifPresent(account -> {
        if (account.getBalance(currency) < amount)
          locale.sendParsedMessage(player, "<red>Insufficient funds.</red>");
        else {
          platform.getDatabase().getByPlayer(target).ifPresent(targetAccount -> {
            account.subtractBalance(currency, amount);
            targetAccount.addBalance(currency, amount);
            locale.sendParsedMessage(player, MessageType.TRANSFER_SEND,
              "sender", player.getName(),
              "receiver", account.getUsername(),
              "amount", String.valueOf(amount),
              "balance", String.valueOf(account.getBalance(currency)));
            Player targetPlayer;
            if (target.isOnline() && (targetPlayer = target.getPlayer()) != null)
              locale.sendParsedMessage(targetPlayer, MessageType.TRANSFER_RECEIVE,
                "sender", player.getName(),
                "receiver", account.getUsername(),
                "amount", String.valueOf(amount),
                "balance", String.valueOf(targetAccount.getBalance(currency)));
          });
        }
      });
    }

    private void updateLeaderboard(@Nullable CommandSender sender, @Nullable CommandArguments args) {
      leaderboard.clear();
      leaderboard.addAll(platform.getDatabase().getEntries());
      leaderboard.sort(Comparator.comparing(account -> account.getBalanceEntry(currency)));
    }

    private void viewLeaderboard(CommandSender sender, CommandArguments args) {
      int page = (int) args.getOrDefault("page", 1);

      int pageSize = 10;
      int maxPage = (int) Math.ceil((double) leaderboard.size() / pageSize);

      int start = (page - 1) * pageSize;
      int end = Math.min(leaderboard.size(), start + pageSize);

      locale.sendParsedMessage(sender, MessageType.LEADERBOARD_TITLE,
        "currency", currency,
        "page", String.valueOf(page),
        "max-page", String.valueOf(maxPage));
      locale.sendParsedMessage(sender, MessageType.LEADERBOARD_SERVER_TOTAL,
        "server-total", String.valueOf(leaderboard.size()));
      for (int i = start; i < end; i++) {
        if (i >= leaderboard.size()) break;
        PlayerAccount account = leaderboard.get(i);
        locale.sendParsedMessage(sender, MessageType.LEADERBOARD_BALANCE_VIEW,
          "placement", String.valueOf(i + 1),
          "player", account.getUsername(),
          "balance", String.valueOf(account.getBalance(currency)));
      }
      locale.sendParsedMessage(sender, MessageType.LEADERBOARD_NEXT_PAGE,
        "command", args.fullInput().split(" ")[0],
        "page", String.valueOf(page),
        "next-page", String.valueOf(page == maxPage ? page : page + 1),
        "max-page", String.valueOf(maxPage));
    }

    private void setBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
      OfflinePlayer target = (OfflinePlayer) args.get("target");
      Double amount = (Double) args.get("amount");

      if (target == null)
        throw CommandAPI.failWithString("Player not found");
      if (amount == null)
        throw CommandAPI.failWithString("Invalid amount");

      platform.getDatabase().getByPlayer(target).ifPresent(account -> {
        locale.sendParsedMessage(sender, MessageType.ADMIN_SET,
          "player", account.getUsername(),
          "old-balance", String.valueOf(account.getBalance(currency)),
          "new-balance", String.valueOf(amount));
        account.setBalance(currency, amount);
      });
    }

    private void addBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
      OfflinePlayer target = (OfflinePlayer) args.get("target");
      Double amount = (Double) args.get("amount");

      if (target == null)
        throw CommandAPI.failWithString("Player not found");
      if (amount == null)
        throw CommandAPI.failWithString("Invalid amount");

      platform.getDatabase().getByPlayer(target).ifPresent(account -> {
        locale.sendParsedMessage(sender, MessageType.ADMIN_ADD,
          "player", account.getUsername(),
          "old-balance", String.valueOf(account.getBalance(currency)),
          "balance-change", String.valueOf(amount),
          "new-balance", String.valueOf(account.getBalance(currency) + amount));
        account.addBalance(currency, amount);
      });
    }

    private void removeBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
      OfflinePlayer target = (OfflinePlayer) args.get("target");
      Double amount = (Double) args.get("amount");

      if (target == null)
        throw CommandAPI.failWithString("Player not found");
      if (amount == null)
        throw CommandAPI.failWithString("Invalid amount");

      platform.getDatabase().getByPlayer(target).ifPresent(account -> {
        locale.sendParsedMessage(sender, MessageType.ADMIN_REMOVE,
          "player", account.getUsername(),
          "old-balance", String.valueOf(account.getBalance(currency)),
          "balance-change", String.valueOf(amount),
          "new-balance", String.valueOf(account.getBalance(currency) - amount));
        account.subtractBalance(currency, amount);
      });
    }

    private void resetBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
      OfflinePlayer target = (OfflinePlayer) args.get("target");

      if (target == null)
        throw CommandAPI.failWithString("Player not found");

      platform.getDatabase().getByPlayer(target).ifPresent(account -> {
        locale.sendParsedMessage(sender, MessageType.ADMIN_RESET,
          "player", account.getUsername(),
          "old-balance", String.valueOf(account.getBalance(currency)));
        account.setBalance(currency, 0);
      });
    }

  }

}
