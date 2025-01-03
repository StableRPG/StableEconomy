package me.jeremiah.economy.currency;

import com.google.common.base.Preconditions;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.jeremiah.economy.EconomyPlatform;
import me.jeremiah.economy.config.messages.Locale;
import me.jeremiah.economy.config.messages.MessageType;
import me.jeremiah.economy.currency.formatting.CurrencyFormatter;
import me.jeremiah.economy.currency.formatting.Formatters;
import me.jeremiah.economy.data.PlayerAccount;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Currency {

  // TODO Further Vault integration
  // TODO Customizable leaderboard page length
  // TODO Customizable leaderboard update interval
  // TODO Starting balance for new players

  private final @NotNull String name;
  private final @NotNull EconomyPlatform platform;
  private final @NotNull Locale locale;

  private final @NotNull CurrencyFormatter formatter;

  private final @Nullable CommandTree viewCommand;

  private final @Nullable CommandTree transferCommand;

  private final @Nullable CommandTree leaderboardCommand;
  private @Nullable List<PlayerAccount> leaderboard;
  private @Nullable ScheduledTask autoUpdateLeaderboard;

  private final @Nullable CommandTree adminCommand;

  private Currency(@NotNull String name, @NotNull EconomyPlatform platform, @Nullable Locale locale,
                   @NotNull Formatters formatter, @NotNull String prefix, @NotNull String suffix,
                   @Nullable String viewCommandName, @Nullable String[] viewCommandAliases, @Nullable String viewCommandPermission,
                   @Nullable String transferCommandName, @Nullable String[] transferCommandAliases, @Nullable String transferCommandPermission,
                   @Nullable String leaderboardCommandName, @Nullable String[] leaderboardCommandAliases, @Nullable String leaderboardCommandPermission,
                   @Nullable String adminCommandName, @Nullable String[] adminCommandAliases, @Nullable String adminCommandPermission) {
    this.name = name;
    this.platform = platform;
    this.locale = locale != null ? locale : platform.getDefaultLocale();
    this.formatter = CurrencyFormatter.of(formatter, prefix, suffix);

    if (viewCommandName != null) {
      viewCommand = new CommandTree(viewCommandName).withAliases(viewCommandAliases);
      if (viewCommandPermission != null) viewCommand.withPermission(viewCommandPermission);
      viewCommand
        .then(new OfflinePlayerArgument("target")
          .replaceSuggestions(ArgumentSuggestions.strings(ignored -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)))
          .executes(this::viewOtherBalance))
        .executesPlayer(this::viewOwnBalance);
    } else viewCommand = null;

    if (transferCommandName != null) {
      transferCommand = new CommandTree(transferCommandName).withAliases(transferCommandAliases);
      if (transferCommandPermission != null) transferCommand.withPermission(transferCommandPermission);
      transferCommand
        .then(new OfflinePlayerArgument("target")
          .replaceSuggestions(ArgumentSuggestions.strings(ignored -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)))
          .then(new DoubleArgument("amount")
            .executesPlayer(this::transferBalance)));
    } else transferCommand = null;

    if (leaderboardCommandName != null) {
      leaderboardCommand = new CommandTree(leaderboardCommandName).withAliases(leaderboardCommandAliases);
      if (leaderboardCommandPermission != null) leaderboardCommand.withPermission(leaderboardCommandPermission);

      String updatePermission;
      if (adminCommandPermission != null) updatePermission = adminCommandPermission;
      else if (leaderboardCommandPermission != null) updatePermission = leaderboardCommandPermission + ".update";
      else updatePermission = "economy.leaderboard.update";

      leaderboardCommand
        .then(LiteralArgument.of("update")
          .withPermission(updatePermission)
          .executes(this::executeLeaderboardUpdate))
        .then(new IntegerArgument("page", 1)
          .setOptional(true)
          .executes(this::viewLeaderboard));
    } else leaderboardCommand = null;

    if (adminCommandName != null) {
      adminCommand = new CommandTree(adminCommandName).withAliases(adminCommandAliases);
      if (adminCommandPermission != null) adminCommand.withPermission(adminCommandPermission);
      adminCommand
        .then(LiteralArgument.of("give")
          .then(new OfflinePlayerArgument("target")
            .replaceSuggestions(ArgumentSuggestions.strings(ignored -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)))
            .then(new DoubleArgument("amount")
              .executes(this::addPlayerBalance))))
        .then(LiteralArgument.of("take")
          .then(new OfflinePlayerArgument("target")
            .replaceSuggestions(ArgumentSuggestions.strings(ignored -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)))
            .then(new DoubleArgument("amount")
              .executes(this::subtractPlayerBalance))))
        .then(LiteralArgument.of("set")
          .then(new OfflinePlayerArgument("target")
            .replaceSuggestions(ArgumentSuggestions.strings(ignored -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)))
            .then(new DoubleArgument("amount")
              .executes(this::setPlayerBalance))))
        .then(LiteralArgument.of("reset")
          .then(new OfflinePlayerArgument("target")
            .replaceSuggestions(ArgumentSuggestions.strings(ignored -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)))
            .executes(this::resetPlayerBalance)));
    } else adminCommand = null;
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

  public @NotNull CurrencyFormatter getFormatter() {
    return formatter;
  }

  public @NotNull String getPrefix() {
    return formatter.getPrefix();
  }

  public @NotNull String getSuffix() {
    return formatter.getSuffix();
  }

  public void register() {
    if (viewCommand != null) viewCommand.register();
    if (transferCommand != null) transferCommand.register();
    if (leaderboardCommand != null) leaderboardCommand.register();
    if (leaderboard != null) this.autoUpdateLeaderboard = Bukkit.getAsyncScheduler().runAtFixedRate(platform.getPlugin(), task -> updateLeaderboard(), 0, 5, TimeUnit.MINUTES);
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

  public String format(double amount) {
    return formatter.format(amount);
  }

  public void updateLeaderboard() {
    leaderboard = platform.getDatabase().sortedByBalance(name);
  }

  // Balance Actions

  public double getBalance(OfflinePlayer player) {
    return platform.getDatabase().getByPlayer(player).map(account -> account.getBalance(name)).orElse(0.0);
  }

  public String getBalanceFormatted(OfflinePlayer player) {
    return format(getBalance(player));
  }

  public double getBalance(UUID uuid) {
    return platform.getDatabase().getByUUID(uuid).map(account -> account.getBalance(name)).orElse(0.0);
  }

  public String getBalanceFormatted(UUID uuid) {
    return format(getBalance(uuid));
  }

  public double getBalance(String username) {
    return platform.getDatabase().getByUsername(username).map(account -> account.getBalance(name)).orElse(0.0);
  }

  public String getBalanceFormatted(String username) {
    return format(getBalance(username));
  }

  public double getBalance(PlayerAccount account) {
    return account.getBalance(name);
  }

  public String getBalanceFormatted(PlayerAccount account) {
    return format(getBalance(account));
  }

  public void setBalance(OfflinePlayer player, double balance) {
    platform.getDatabase().updateByPlayer(player, playerAccount -> playerAccount.setBalance(name, balance));
  }

  public void setBalance(UUID uuid, double balance) {
    platform.getDatabase().updateByUUID(uuid, account -> account.setBalance(name, balance));
  }

  public void setBalance(String username, double balance) {
    platform.getDatabase().updateByUsername(username, account -> account.setBalance(name, balance));
  }

  public void setBalance(PlayerAccount account, double balance) {
    account.setBalance(name, balance);
  }

  public void addBalance(OfflinePlayer player, double amount) {
    platform.getDatabase().updateByPlayer(player, account -> addBalance(account, amount));
  }

  public void addBalance(UUID uuid, double amount) {
    platform.getDatabase().updateByUUID(uuid, account -> addBalance(account, amount));
  }

  public void addBalance(String username, double amount) {
    platform.getDatabase().updateByUsername(username, account -> addBalance(account, amount));
  }

  public void addBalance(PlayerAccount account, double amount) {
    account.addBalance(name, amount);
  }

  public void subtractBalance(OfflinePlayer player, double amount) {
    platform.getDatabase().updateByPlayer(player, account -> subtractBalance(account, amount));
  }

  public void subtractBalance(UUID uuid, double amount) {
    platform.getDatabase().updateByUUID(uuid, account -> subtractBalance(account, amount));
  }

  public void subtractBalance(String username, double amount) {
    platform.getDatabase().updateByUsername(username, account -> subtractBalance(account, amount));
  }

  public void subtractBalance(PlayerAccount account, double amount) {
    account.subtractBalance(name, amount);
  }

  public void resetBalance(OfflinePlayer player) {
    platform.getDatabase().updateByPlayer(player, this::resetBalance);
  }

  public void resetBalance(UUID uuid) {
    platform.getDatabase().updateByUUID(uuid, this::resetBalance);
  }

  public void resetBalance(String username) {
    platform.getDatabase().updateByUsername(username, this::resetBalance);
  }

  public void resetBalance(PlayerAccount account) {
    account.setBalance(name, 0);
  }

  // Command Actions

  private void viewOtherBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    OfflinePlayer target = (OfflinePlayer) args.get("target");

    if (target == null)
      throw CommandAPI.failWithString("Player not found");

    platform.getDatabase().getByPlayer(target).ifPresent(account -> locale.sendParsedMessage(sender, MessageType.VIEW_OTHER,
      "player", account.getUsername(),
      "balance", getBalanceFormatted(account)));
  }

  private void viewOwnBalance(Player player, CommandArguments args) {
    platform.getDatabase().getByPlayer(player).ifPresent(account -> locale.sendParsedMessage(player, MessageType.VIEW_OWN,
      "balance", getBalanceFormatted(account)));
  }

  private void transferBalance(Player player, CommandArguments args) throws WrapperCommandSyntaxException {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    Double amount = (Double) args.get("amount");

    if (target == null)
      throw CommandAPI.failWithString("Player not found");
    if (amount == null)
      throw CommandAPI.failWithString("Invalid amount");

    platform.getDatabase().getByPlayer(player).ifPresent(account -> {
      if (getBalance(account) < amount)
        locale.sendParsedMessage(player, "<red>Insufficient funds.</red>");
      else {
        platform.getDatabase().getByPlayer(target).ifPresent(targetAccount -> {
          locale.sendParsedMessage(player, MessageType.TRANSFER_SEND,
            "sender", player.getName(),
            "receiver", account.getUsername(),
            "amount", format(amount),
            "old-balance", getBalanceFormatted(account),
            "new-balance", format(getBalance(account) - amount));
          Player targetPlayer;
          if (target.isOnline() && (targetPlayer = target.getPlayer()) != null)
            locale.sendParsedMessage(targetPlayer, MessageType.TRANSFER_RECEIVE,
              "sender", player.getName(),
              "receiver", account.getUsername(),
              "amount", format(amount),
              "old-balance", getBalanceFormatted(targetAccount),
              "new-balance", format(getBalance(targetAccount) + amount));
          subtractBalance(account, amount);
          addBalance(targetAccount, amount);
        });
      }
    });
  }

  private void executeLeaderboardUpdate(@Nullable CommandSender sender, @Nullable CommandArguments args) {
    updateLeaderboard();
  }

  private void viewLeaderboard(CommandSender sender, CommandArguments args) {
    int page = (int) args.getOrDefault("page", 1);

    int pageSize = 10;
    int maxPage = (int) Math.ceil((double) leaderboard.size() / pageSize);

    int start = (page - 1) * pageSize;
    int end = Math.min(leaderboard.size(), start + pageSize);

    locale.sendParsedMessage(sender, MessageType.LEADERBOARD_TITLE,
      "currency", name,
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
        "balance", getBalanceFormatted(account));
    }
    locale.sendParsedMessage(sender, MessageType.LEADERBOARD_NEXT_PAGE,
      "command", args.fullInput().split(" ")[0],
      "page", String.valueOf(page),
      "next-page", String.valueOf(page == maxPage ? page : page + 1),
      "max-page", String.valueOf(maxPage));
  }

  private void setPlayerBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    Double amount = (Double) args.get("amount");

    if (target == null)
      throw CommandAPI.failWithString("Player not found");
    if (amount == null)
      throw CommandAPI.failWithString("Invalid amount");

    platform.getDatabase().getByPlayer(target).ifPresent(account -> {
      locale.sendParsedMessage(sender, MessageType.ADMIN_SET,
        "player", account.getUsername(),
        "old-balance", getBalanceFormatted(account),
        "new-balance", format(amount));
      setBalance(account, amount);
    });
  }

  private void addPlayerBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    Double amount = (Double) args.get("amount");

    if (target == null)
      throw CommandAPI.failWithString("Player not found");
    if (amount == null)
      throw CommandAPI.failWithString("Invalid amount");

    platform.getDatabase().getByPlayer(target).ifPresent(account -> {
      locale.sendParsedMessage(sender, MessageType.ADMIN_ADD,
        "player", account.getUsername(),
        "old-balance", getBalanceFormatted(account),
        "balance-change", format(amount),
        "new-balance", format(getBalance(account) + amount));
      addBalance(account, amount);
    });
  }

  private void subtractPlayerBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    Double amount = (Double) args.get("amount");

    if (target == null)
      throw CommandAPI.failWithString("Player not found");
    if (amount == null)
      throw CommandAPI.failWithString("Invalid amount");

    platform.getDatabase().getByPlayer(target).ifPresent(account -> {
      locale.sendParsedMessage(sender, MessageType.ADMIN_REMOVE,
        "player", account.getUsername(),
        "old-balance", getBalanceFormatted(account),
        "balance-change", format(amount),
        "new-balance", format(getBalance(account) - amount));
      subtractBalance(account, amount);
    });
  }

  private void resetPlayerBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    OfflinePlayer target = (OfflinePlayer) args.get("target");

    if (target == null)
      throw CommandAPI.failWithString("Player not found");

    platform.getDatabase().getByPlayer(target).ifPresent(account -> {
      locale.sendParsedMessage(sender, MessageType.ADMIN_RESET,
        "player", account.getUsername(),
        "old-balance", getBalanceFormatted(account));
      resetBalance(account);
    });
  }

  public static class Builder {

    private final String currency;
    private final EconomyPlatform platform;

    private Locale locale;

    private Formatters formatter = Formatters.COOL;
    private String prefix = "";
    private String suffix = "";

    private String viewCommandName;
    private String[] viewCommandAliases = new String[0];
    private String viewCommandPermission;

    private String transferCommandName;
    private String[] transferCommandAliases = new String[0];
    private String transferCommandPermission;

    private String leaderboardCommandName;
    private String[] leaderboardCommandAliases = new String[0];
    private String leaderboardCommandPermission;

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
      if (config.contains("view-command")) {
        ConfigurationSection viewConfig = config.getConfigurationSection("view-command");
        assert viewConfig != null;
        viewCommandName = viewConfig.getString("name");
        viewCommandAliases = viewConfig.getStringList("aliases").toArray(new String[0]);
        viewCommandPermission = viewConfig.getString("permission");
      }
      if (config.contains("transfer-command")) {
        ConfigurationSection transferConfig = config.getConfigurationSection("transfer-command");
        assert transferConfig != null;
        transferCommandName = transferConfig.getString("name");
        transferCommandAliases = transferConfig.getStringList("aliases").toArray(new String[0]);
        transferCommandPermission = transferConfig.getString("permission");
      }
      if (config.contains("leaderboard-command")) {
        ConfigurationSection leaderboardConfig = config.getConfigurationSection("leaderboard-command");
        assert leaderboardConfig != null;
        leaderboardCommandName = leaderboardConfig.getString("name");
        leaderboardCommandAliases = leaderboardConfig.getStringList("aliases").toArray(new String[0]);
        leaderboardCommandPermission = leaderboardConfig.getString("permission");
      }
      if (config.contains("admin-command")) {
        ConfigurationSection adminConfig = config.getConfigurationSection("admin-command");
        assert adminConfig != null;
        adminCommandName = adminConfig.getString("name");
        adminCommandAliases = adminConfig.getStringList("aliases").toArray(new String[0]);
        adminCommandPermission = adminConfig.getString("permission");
      }
      formatter = Formatters.fromString(config.getString("formatter", "cool"));
      prefix = config.getString("prefix", "");
      suffix = config.getString("suffix", "");
      return this;
    }

    public Builder withLocale(@NotNull Locale locale) {
      this.locale = locale;
      return this;
    }

    public Builder withFormatter(@NotNull Formatters formatter) {
      this.formatter = formatter;
      return this;
    }

    public Builder withPrefix(@NotNull String prefix) {
      this.prefix = prefix;
      return this;
    }

    public Builder withSuffix(@NotNull String suffix) {
      this.suffix = suffix;
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
      return new Currency(
        currency, platform, locale,
        formatter, prefix, suffix,
        viewCommandName, viewCommandAliases, viewCommandPermission,
        transferCommandName, transferCommandAliases, transferCommandPermission,
        leaderboardCommandName, leaderboardCommandAliases, leaderboardCommandPermission,
        adminCommandName, adminCommandAliases, adminCommandPermission
      );
    }

  }

}
