package me.jeremiah.stableeconomy.currency;

import com.google.common.base.Preconditions;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.Getter;
import me.jeremiah.stableeconomy.EconomyPlatform;
import me.jeremiah.stableeconomy.command.AccountArgument;
import me.jeremiah.stableeconomy.config.messages.Locale;
import me.jeremiah.stableeconomy.config.messages.MessageType;
import me.jeremiah.stableeconomy.currency.formatting.CurrencyFormatter;
import me.jeremiah.stableeconomy.currency.formatting.Formatters;
import me.jeremiah.stableeconomy.data.PlayerAccount;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Currency {

  @Getter
  private final @NotNull String id;
  @Getter
  private final @NotNull EconomyPlatform platform;
  @Getter
  private final @NotNull Locale locale;

  @Getter
  private final String singularDisplayName;
  @Getter
  private final String pluralDisplayName;

  @Getter
  private final double startingBalance;

  @Getter
  private final @NotNull CurrencyFormatter formatter;

  private final @Nullable CommandTree viewCommand;
  private final @Nullable CommandTree transferCommand;

  private final @Nullable CommandTree leaderboardCommand;
  private final int leaderboardPageLength;
  private final long leaderboardUpdateInterval;
  private volatile @Nullable List<PlayerAccount> leaderboard;
  private @Nullable ScheduledFuture<?> updateLeaderboardTask;

  private final @Nullable CommandTree adminCommand;

  private Currency(@NotNull String id, @NotNull EconomyPlatform platform, @Nullable Locale locale,
                   @NotNull String singularDisplayName, @NotNull String pluralDisplayName, double startingBalance,
                   @NotNull Formatters formatter, @NotNull String formatString,
                   @NotNull Command viewCommand, @NotNull Command transferCommand,
                   @NotNull Command leaderboardCommand, int leaderboardPageLength, long leaderboardUpdateInterval,
                   @NotNull Command adminCommand) {
    this.id = id;
    this.platform = platform;
    this.locale = locale != null ? locale : platform.getDefaultLocale();
    this.singularDisplayName = singularDisplayName;
    this.pluralDisplayName = pluralDisplayName;
    this.startingBalance = startingBalance;
    this.formatter = CurrencyFormatter.of(formatter, formatString);

    if (viewCommand.canBeCreated()) {
      this.viewCommand = new CommandTree(viewCommand.name()).withAliases(viewCommand.aliases());
      if (viewCommand.hasPermission()) this.viewCommand.withPermission(viewCommand.permission());
      this.viewCommand
        .then(new AccountArgument(this)
          .executes(this::viewOtherBalance))
        .executesPlayer(this::viewOwnBalance);
    } else this.viewCommand = null;

    if (transferCommand.canBeCreated()) {
      this.transferCommand = new CommandTree(transferCommand.name()).withAliases(transferCommand.aliases());
      if (transferCommand.hasPermission()) this.transferCommand.withPermission(transferCommand.permission());
      this.transferCommand
        .then(new AccountArgument(this)
          .then(new DoubleArgument("amount")
            .executesPlayer(this::transferBalance)));
    } else this.transferCommand = null;

    if (leaderboardCommand.canBeCreated()) {
      this.leaderboardCommand = new CommandTree(leaderboardCommand.name()).withAliases(leaderboardCommand.aliases());
      if (leaderboardCommand.hasPermission()) this.leaderboardCommand.withPermission(leaderboardCommand.permission());

      String updatePermission;
      if (adminCommand.hasPermission()) updatePermission = adminCommand.permission();
      else if (leaderboardCommand.hasPermission()) updatePermission = leaderboardCommand.permission() + ".update";
      else updatePermission = "economy.leaderboard.update";

      this.leaderboardCommand
        .then(LiteralArgument.of("update")
          .withPermission(updatePermission)
          .executes(this::executeLeaderboardUpdate))
        .then(new IntegerArgument("page", 1)
          .setOptional(true)
          .executes(this::viewLeaderboard));

      this.leaderboard = new ArrayList<>();
    } else this.leaderboardCommand = null;
    this.leaderboardPageLength = leaderboardPageLength;
    this.leaderboardUpdateInterval = leaderboardUpdateInterval;

    if (adminCommand.canBeCreated()) {
      this.adminCommand = new CommandTree(adminCommand.name()).withAliases(adminCommand.aliases());
      if (adminCommand.hasPermission()) this.adminCommand.withPermission(adminCommand.permission());
      this.adminCommand
        .then(LiteralArgument.of("give")
          .then(new AccountArgument(this)
            .then(new DoubleArgument("amount")
              .executes(this::addPlayerBalance))))
        .then(LiteralArgument.of("take")
          .then(new AccountArgument(this)
            .then(new DoubleArgument("amount")
              .executes(this::subtractPlayerBalance))))
        .then(LiteralArgument.of("set")
          .then(new AccountArgument(this)
            .then(new DoubleArgument("amount")
              .executes(this::setPlayerBalance))))
        .then(LiteralArgument.of("reset")
          .then(new AccountArgument(this)
            .executes(this::resetPlayerBalance)));
    } else this.adminCommand = null;
  }

  public boolean isDefaultCurrency() {
    return id.equals("default");
  }

  public void register() {
    if (viewCommand != null) viewCommand.register();
    if (transferCommand != null) transferCommand.register();
    if (leaderboardCommand != null) leaderboardCommand.register();
    if (leaderboard != null)
      this.updateLeaderboardTask = getPlatform().getScheduler().scheduleAtFixedRate(this::updateLeaderboard, 0, leaderboardUpdateInterval, TimeUnit.SECONDS);
    if (adminCommand != null) adminCommand.register();
  }

  public void unregister() {
    if (viewCommand != null) CommandAPI.unregister(viewCommand.getName());
    if (transferCommand != null) CommandAPI.unregister(transferCommand.getName());
    if (leaderboardCommand != null) CommandAPI.unregister(leaderboardCommand.getName());
    if (updateLeaderboardTask != null) {
      updateLeaderboardTask.cancel(true);
      updateLeaderboardTask = null;
    }
    if (leaderboard != null) leaderboard = null;
    if (adminCommand != null) CommandAPI.unregister(adminCommand.getName());
  }

  public String format(double amount) {
    return formatter.format(amount);
  }

  public void updateLeaderboard() {
    leaderboard = platform.getDatabase().sortedByBalance(id);
  }

  public PlayerAccount getLeaderboardEntry(int position) {
    List<PlayerAccount> leaderboard = this.leaderboard;
    if (leaderboard == null || position < 0 || position >= leaderboard.size()) return null;
    return leaderboard.get(position);
  }

  // Balance Actions

  public double getBalance(OfflinePlayer player) {
    return platform.getDatabase().getByPlayer(player).map(account -> account.getBalance(id)).orElse(0.0);
  }

  public String getBalanceFormatted(OfflinePlayer player) {
    return format(getBalance(player));
  }

  public double getBalance(UUID uuid) {
    return platform.getDatabase().getByUUID(uuid).map(account -> account.getBalance(id)).orElse(0.0);
  }

  public String getBalanceFormatted(UUID uuid) {
    return format(getBalance(uuid));
  }

  public double getBalance(String username) {
    return platform.getDatabase().getByUsername(username).map(account -> account.getBalance(id)).orElse(0.0);
  }

  public String getBalanceFormatted(String username) {
    return format(getBalance(username));
  }

  public double getBalance(PlayerAccount account) {
    return account.getBalance(id);
  }

  public String getBalanceFormatted(PlayerAccount account) {
    return format(getBalance(account));
  }

  public void setBalance(OfflinePlayer player, double balance) {
    platform.getDatabase().updateByPlayer(player, playerAccount -> playerAccount.setBalance(id, balance));
  }

  public void setBalance(UUID uuid, double balance) {
    platform.getDatabase().updateByUUID(uuid, account -> account.setBalance(id, balance));
  }

  public void setBalance(String username, double balance) {
    platform.getDatabase().updateByUsername(username, account -> account.setBalance(id, balance));
  }

  public void setBalance(PlayerAccount account, double balance) {
    account.setBalance(id, balance);
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
    account.addBalance(id, amount);
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
    account.subtractBalance(id, amount);
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
    account.setBalance(id, 0);
  }

  // Command Actions

  private void viewOtherBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    PlayerAccount target = (PlayerAccount) args.get("target");

    if (target == null)
      throw CommandAPI.failWithString("Player not found");


    locale.sendParsedMessage(sender, MessageType.VIEW_OTHER,
      "player", target.getUsername(),
      "balance", getBalanceFormatted(target));
  }

  private void viewOwnBalance(Player player, CommandArguments args) {
    platform.getDatabase().getByPlayer(player).ifPresent(account -> locale.sendParsedMessage(player, MessageType.VIEW_OWN,
      "balance", getBalanceFormatted(account)));
  }

  private void transferBalance(Player player, CommandArguments args) throws WrapperCommandSyntaxException {
    PlayerAccount target = (PlayerAccount) args.get("target");
    Double amount = (Double) args.get("amount");

    if (target == null)
      throw CommandAPI.failWithString("Player not found");
    if (amount == null)
      throw CommandAPI.failWithString("Invalid amount");

    PlayerAccount account = platform.getDatabase().getByPlayer(player)
      .orElseThrow(() -> CommandAPI.failWithString("You do not have an account"));

    if (getBalance(account) < amount)
      throw CommandAPI.failWithString("Insufficient funds");

    locale.sendParsedMessage(player, MessageType.TRANSFER_SEND,
      "sender", player.getName(),
      "receiver", account.getUsername(),
      "amount", format(amount),
      "old-balance", getBalanceFormatted(account),
      "new-balance", format(getBalance(account) - amount));

    Player targetPlayer = Bukkit.getPlayer(target.getUniqueId());
    if (targetPlayer != null)
      locale.sendParsedMessage(targetPlayer, MessageType.TRANSFER_RECEIVE,
        "sender", player.getName(),
        "receiver", account.getUsername(),
        "amount", format(amount),
        "old-balance", getBalanceFormatted(target),
        "new-balance", format(getBalance(target) + amount));

    subtractBalance(account, amount);
    addBalance(target, amount);
  }

  private void executeLeaderboardUpdate(@Nullable CommandSender sender, @Nullable CommandArguments args) {
    updateLeaderboard();
  }

  private void viewLeaderboard(CommandSender sender, CommandArguments args) {
    List<PlayerAccount> leaderboard = this.leaderboard;
    assert leaderboard != null;
    int page = (int) args.getOrDefault("page", 1);

    int maxPage = (int) Math.ceil((double) leaderboard.size() / leaderboardPageLength);

    int start = (page - 1) * leaderboardPageLength;
    int end = Math.min(leaderboard.size(), start + leaderboardPageLength);

    locale.sendParsedMessage(sender, MessageType.LEADERBOARD_TITLE,
      "currency", id,
      "page", String.valueOf(page),
      "max-page", String.valueOf(maxPage));
    locale.sendParsedMessage(sender, MessageType.LEADERBOARD_SERVER_TOTAL,
      "server-total", String.valueOf(leaderboard.size()));
    for (int i = start; i < end; i++) {
      if (i >= leaderboard.size()) break;
      PlayerAccount account = leaderboard.get(i);
      locale.sendParsedMessage(sender, MessageType.LEADERBOARD_BALANCE_VIEW,
        "position", String.valueOf(i + 1),
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
    PlayerAccount target = (PlayerAccount) args.get("target");
    Double amount = (Double) args.get("amount");

    if (target == null)
      throw CommandAPI.failWithString("Player not found");
    if (amount == null)
      throw CommandAPI.failWithString("Invalid amount");

    locale.sendParsedMessage(sender, MessageType.ADMIN_SET,
      "player", target.getUsername(),
      "old-balance", getBalanceFormatted(target),
      "new-balance", format(amount)
    );

    setBalance(target, amount);
  }

  private void addPlayerBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    PlayerAccount target = (PlayerAccount) args.get("target");
    Double amount = (Double) args.get("amount");

    if (target == null)
      throw CommandAPI.failWithString("Player not found");
    if (amount == null)
      throw CommandAPI.failWithString("Invalid amount");

    locale.sendParsedMessage(sender, MessageType.ADMIN_ADD,
      "player", target.getUsername(),
      "old-balance", getBalanceFormatted(target),
      "balance-change", format(amount),
      "new-balance", format(getBalance(target) + amount)
    );

    addBalance(target, amount);
  }

  private void subtractPlayerBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    PlayerAccount target = (PlayerAccount) args.get("target");
    Double amount = (Double) args.get("amount");

    if (target == null)
      throw CommandAPI.failWithString("Player not found");
    if (amount == null)
      throw CommandAPI.failWithString("Invalid amount");

    locale.sendParsedMessage(sender, MessageType.ADMIN_REMOVE,
      "player", target.getUsername(),
      "old-balance", getBalanceFormatted(target),
      "balance-change", format(amount),
      "new-balance", format(getBalance(target) - amount)
    );

    subtractBalance(target, amount);
  }

  private void resetPlayerBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    PlayerAccount target = (PlayerAccount) args.get("target");

    if (target == null)
      throw CommandAPI.failWithString("Player not found");

    locale.sendParsedMessage(sender, MessageType.ADMIN_RESET,
      "player", target.getUsername(),
      "old-balance", getBalanceFormatted(target)
    );

    resetBalance(target);
  }

  public static class Builder {

    private final String currency;
    private final EconomyPlatform platform;

    private Locale locale;

    private String singularDisplayName;
    private String pluralDisplayName;

    private double startingBalance = 0.0;

    private Formatters formatter = Formatters.COOL;
    private String formatString = "";

    private final Command viewCommand = new Command();
    private final Command transferCommand = new Command();

    private final Command leaderboardCommand = new Command();
    private int leaderboardPageLength = 10;
    private long leaderboardUpdateInterval = 300;

    private final Command adminCommand = new Command();

    public Builder(@NotNull String currency, @NotNull EconomyPlatform platform) {
      Preconditions.checkNotNull(currency, "Currency name cannot be null");
      this.currency = currency;
      this.platform = platform;
      this.locale = platform.getDefaultLocale();
    }

    public Builder usingYaml(@NotNull YamlConfiguration config) {
      String capitalCurrency = currency.replaceFirst("^\\w", String.valueOf(Character.toUpperCase(currency.charAt(0))));
      singularDisplayName = config.getString("singular-display-name", capitalCurrency);
      pluralDisplayName = config.getString("plural-display-name", singularDisplayName + "s");
      startingBalance = config.getDouble("starting-balance", 0.0);
      if (config.contains("view-command"))
        viewCommand.usingYaml(config.getConfigurationSection("view-command"));
      if (config.contains("transfer-command"))
        transferCommand.usingYaml(config.getConfigurationSection("transfer-command"));
      if (config.contains("leaderboard-command")) {
        ConfigurationSection section = config.getConfigurationSection("leaderboard-command");
        leaderboardCommand.usingYaml(section);
        if (section != null) {
          leaderboardPageLength = section.getInt("page-length", 10);
          leaderboardUpdateInterval = section.getLong("update-interval", 300);
        }
      }
      if (config.contains("admin-command"))
        adminCommand.usingYaml(config.getConfigurationSection("admin-command"));
      formatter = Formatters.fromString(config.getString("formatter", "cool"));
      formatString = config.getString("format-string", "");
      return this;
    }

    public Builder withLocale(@NotNull Locale locale) {
      this.locale = locale;
      return this;
    }

    public Builder withDisplayName(@NotNull String singular, @NotNull String plural) {
      return withSingularDisplayName(singular).withPluralDisplayName(plural);
    }

    public Builder withSingularDisplayName(@NotNull String singular) {
      this.singularDisplayName = singular;
      if (pluralDisplayName == null) pluralDisplayName = singular + "s";
      return this;
    }

    public Builder withPluralDisplayName(@NotNull String plural) {
      this.pluralDisplayName = plural;
      return this;
    }

    public Builder withStartingBalance(double balance) {
      this.startingBalance = balance;
      return this;
    }

    public Builder withFormatter(@NotNull Formatters formatter) {
      this.formatter = formatter;
      return this;
    }

    public Builder withFormattingString(@NotNull String formatString) {
      this.formatString = formatString;
      return this;
    }

    public Builder withViewCommandName(@NotNull String name) {
      viewCommand.name(name);
      return this;
    }

    public Builder withViewCommandAliases(@NotNull String @NotNull ... aliases) {
      viewCommand.aliases(aliases);
      return this;
    }

    public Builder withViewCommandPermission(@NotNull String permission) {
      viewCommand.permission(permission);
      return this;
    }

    public Builder withTransferCommandName(@NotNull String name) {
      transferCommand.name(name);
      return this;
    }

    public Builder withTransferCommandAliases(@NotNull String @NotNull ... aliases) {
      transferCommand.aliases(aliases);
      return this;
    }

    public Builder withTransferCommandPermission(@NotNull String permission) {
      transferCommand.permission(permission);
      return this;
    }

    public Builder withLeaderboardCommandName(@NotNull String name) {
      leaderboardCommand.name(name);
      return this;
    }

    public Builder withLeaderboardCommandAliases(@NotNull String @NotNull ... aliases) {
      leaderboardCommand.aliases(aliases);
      return this;
    }

    public Builder withLeaderboardCommandPermission(@NotNull String permission) {
      leaderboardCommand.permission(permission);
      return this;
    }

    public Builder withLeaderboardPageLength(int length) {
      this.leaderboardPageLength = length;
      return this;
    }

    public Builder withLeaderboardUpdateInterval(long interval) {
      this.leaderboardUpdateInterval = interval;
      return this;
    }

    public Builder withAdminCommandName(@NotNull String name) {
      adminCommand.name(name);
      return this;
    }

    public Builder withAdminCommandAliases(@NotNull String @NotNull ... aliases) {
      adminCommand.aliases(aliases);
      return this;
    }

    public Builder withAdminCommandPermission(@NotNull String permission) {
      adminCommand.permission(permission);
      return this;
    }

    public Currency build() {
      return new Currency(
        currency, platform, locale,
        singularDisplayName, pluralDisplayName, startingBalance,
        formatter, formatString,
        viewCommand, transferCommand,
        leaderboardCommand, leaderboardPageLength, leaderboardUpdateInterval,
        adminCommand
      );
    }

  }

  private static class Command {

    private String name = "";
    private String[] aliases = new String[0];
    private String permission = "";

    public void usingYaml(@Nullable ConfigurationSection config) {
      if (config == null) return;
      name = config.getString("name", "");
      aliases = config.getStringList("aliases").toArray(new String[0]);
      permission = config.getString("permission", "");
    }

    public void name(String name) {
      this.name = name;
    }

    public String name() {
      return name;
    }

    public void aliases(String... aliases) {
      this.aliases = aliases;
    }

    public String[] aliases() {
      return aliases;
    }

    public void permission(String permission) {
      this.permission = permission;
    }

    public String permission() {
      return permission;
    }

    public boolean canBeCreated() {
      return !name.isEmpty();
    }

    public boolean hasPermission() {
      return !permission.isEmpty();
    }

  }

}
