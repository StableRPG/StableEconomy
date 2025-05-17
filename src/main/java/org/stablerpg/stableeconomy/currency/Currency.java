package org.stablerpg.stableeconomy.currency;

import com.google.common.base.Preconditions;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.commands.CurrencyCommand;
import org.stablerpg.stableeconomy.commands.arguments.AccountArgument;
import org.stablerpg.stableeconomy.config.messages.Locale;
import org.stablerpg.stableeconomy.config.messages.MessageType;
import org.stablerpg.stableeconomy.currency.formatting.CurrencyFormatter;
import org.stablerpg.stableeconomy.currency.formatting.Formatters;
import org.stablerpg.stableeconomy.data.PlayerAccount;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
  private final @Nullable CommandTree adminCommand;
  private @Nullable List<PlayerAccount> leaderboard;
  private long lastLeaderboardUpdate = 0;

  private Currency(@NotNull String id, @NotNull EconomyPlatform platform, @Nullable Locale locale, @NotNull String singularDisplayName, @NotNull String pluralDisplayName, double startingBalance, @NotNull CurrencyFormatter formatter, @NotNull CurrencyCommand viewCommand, @NotNull CurrencyCommand transferCommand, @NotNull CurrencyCommand leaderboardCommand, int leaderboardPageLength, long leaderboardUpdateInterval, @NotNull CurrencyCommand adminCommand) {
    this.id = id;
    this.platform = platform;
    this.locale = locale != null ? locale : platform.getDefaultLocale();
    this.singularDisplayName = singularDisplayName;
    this.pluralDisplayName = pluralDisplayName;
    this.startingBalance = startingBalance;
    this.formatter = formatter;

    if (viewCommand.canBeCreated()) {
      this.viewCommand = new CommandTree(viewCommand.name()).withAliases(viewCommand.aliases());
      if (viewCommand.hasPermission()) this.viewCommand.withPermission(viewCommand.permission());
      this.viewCommand.then(new AccountArgument(this).executes(this::viewOtherBalance)).executesPlayer(this::viewOwnBalance);
    } else this.viewCommand = null;

    if (transferCommand.canBeCreated()) {
      this.transferCommand = new CommandTree(transferCommand.name()).withAliases(transferCommand.aliases());
      if (transferCommand.hasPermission()) this.transferCommand.withPermission(transferCommand.permission());
      this.transferCommand.then(new AccountArgument(this).then(new DoubleArgument("amount").executesPlayer(this::transferBalance)));
    } else this.transferCommand = null;

    if (leaderboardCommand.canBeCreated()) {
      this.leaderboardCommand = new CommandTree(leaderboardCommand.name()).withAliases(leaderboardCommand.aliases());
      if (leaderboardCommand.hasPermission()) this.leaderboardCommand.withPermission(leaderboardCommand.permission());

      String updatePermission;
      if (adminCommand.hasPermission()) updatePermission = adminCommand.permission();
      else if (leaderboardCommand.hasPermission()) updatePermission = leaderboardCommand.permission() + ".update";
      else updatePermission = "economy.leaderboard.update";

      this.leaderboardCommand.then(LiteralArgument.of("update").withPermission(updatePermission).executes(this::executeLeaderboardUpdate)).then(new IntegerArgument("page", 1).setOptional(true).executes(this::viewLeaderboard));

      this.leaderboard = new ArrayList<>();
    } else this.leaderboardCommand = null;
    this.leaderboardPageLength = leaderboardPageLength;
    this.leaderboardUpdateInterval = leaderboardUpdateInterval;

    if (adminCommand.canBeCreated()) {
      this.adminCommand = new CommandTree(adminCommand.name()).withAliases(adminCommand.aliases());
      if (adminCommand.hasPermission()) this.adminCommand.withPermission(adminCommand.permission());
      this.adminCommand.then(LiteralArgument.of("give").then(new AccountArgument(this).then(new DoubleArgument("amount").executes(this::addPlayerBalance)))).then(LiteralArgument.of("take").then(new AccountArgument(this).then(new DoubleArgument("amount").executes(this::subtractPlayerBalance)))).then(LiteralArgument.of("set").then(new AccountArgument(this).then(new DoubleArgument("amount").executes(this::setPlayerBalance)))).then(LiteralArgument.of("reset").then(new AccountArgument(this).executes(this::resetPlayerBalance)));
    } else this.adminCommand = null;
  }

  public boolean isDefaultCurrency() {
    return id.equals("default");
  }

  public void register() {
    if (viewCommand != null) viewCommand.register();
    if (transferCommand != null) transferCommand.register();
    if (leaderboardCommand != null) leaderboardCommand.register();
    if (adminCommand != null) adminCommand.register();
  }

  public void unregister() {
    if (viewCommand != null) CommandAPI.unregister(viewCommand.getName());
    if (transferCommand != null) CommandAPI.unregister(transferCommand.getName());
    if (leaderboardCommand != null) CommandAPI.unregister(leaderboardCommand.getName());
    if (adminCommand != null) CommandAPI.unregister(adminCommand.getName());
    leaderboard = null;
  }

  public PlayerAccount getLeaderboardEntry(int position) {
    List<PlayerAccount> leaderboard = getLeaderboard();
    if (leaderboard == null || position < 0 || position >= leaderboard.size()) return null;
    return leaderboard.get(position);
  }

  public List<PlayerAccount> getLeaderboard() {
    long currentTime = System.currentTimeMillis();
    if (lastLeaderboardUpdate == 0 || currentTime - lastLeaderboardUpdate >= leaderboardUpdateInterval * 1000) {
      updateLeaderboard();
      lastLeaderboardUpdate = currentTime;
    }
    return leaderboard;
  }

  public void updateLeaderboard() {
    leaderboard = platform.getLeaderboard(id);
  }

  public double getBalance(OfflinePlayer player) {
    return platform.getBalance(player);
  }

  // Balance Actions

  public String getBalanceFormatted(OfflinePlayer player) {
    return format(getBalance(player));
  }

  public String getBalanceFormatted(UUID uuid) {
    return format(getBalance(uuid));
  }

  public String format(double amount) {
    return formatter.format(amount);
  }

  public double getBalance(UUID uuid) {
    return platform.getBalance(uuid, id);
  }

  public String getBalanceFormatted(String username) {
    return format(getBalance(username));
  }

  public double getBalance(String username) {
    return platform.getBalance(username, id);
  }

  public double getBalance(PlayerAccount account) {
    return account.getBalance(id);
  }

  public String getBalanceFormatted(PlayerAccount account) {
    return format(getBalance(account));
  }

  public void setBalance(OfflinePlayer player, double balance) {
    platform.setBalance(player, balance, id);
  }

  public void setBalance(UUID uuid, double balance) {
    platform.setBalance(uuid, balance, id);
  }

  public void setBalance(String username, double balance) {
    platform.setBalance(username, balance, id);
  }

  public void setBalance(PlayerAccount account, double balance) {
    account.setBalance(id, balance);
  }

  public void addBalance(OfflinePlayer player, double amount) {
    platform.addBalance(player, amount, id);
  }

  public void addBalance(UUID uuid, double amount) {
    platform.addBalance(uuid, amount, id);
  }

  public void addBalance(String username, double amount) {
    platform.addBalance(username, amount, id);
  }

  public void addBalance(PlayerAccount account, double amount) {
    account.addBalance(id, amount);
  }

  public void subtractBalance(OfflinePlayer player, double amount) {
    platform.subtractBalance(player, amount, id);
  }

  public void subtractBalance(UUID uuid, double amount) {
    platform.subtractBalance(uuid, amount, id);
  }

  public void subtractBalance(String username, double amount) {
    platform.subtractBalance(username, amount, id);
  }

  public void subtractBalance(PlayerAccount account, double amount) {
    account.subtractBalance(id, amount);
  }

  public void resetBalance(OfflinePlayer player) {
    platform.resetBalance(player, id);
  }

  public void resetBalance(UUID uuid) {
    platform.resetBalance(uuid, id);
  }

  public void resetBalance(String username) {
    platform.resetBalance(username, id);
  }

  public void resetBalance(PlayerAccount account) {
    account.resetBalance(id);
  }

  // Command Actions

  private void viewOtherBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    PlayerAccount target = (PlayerAccount) args.get("target");

    if (target == null) throw CommandAPI.failWithString("Player not found");

    locale.sendParsedMessage(sender, MessageType.VIEW_OTHER, "player", target.getUsername(), "balance", getBalanceFormatted(target));
  }

  private void viewOwnBalance(Player player, CommandArguments args) {
    locale.sendParsedMessage(player, MessageType.VIEW_OWN, "balance", getBalanceFormatted(player));
  }

  private void transferBalance(Player player, CommandArguments args) throws WrapperCommandSyntaxException {
    PlayerAccount target = (PlayerAccount) args.get("target");
    Double amount = (Double) args.get("amount");

    if (target == null) throw CommandAPI.failWithString("Player not found");
    if (amount == null) throw CommandAPI.failWithString("Invalid amount");

    PlayerAccount account = platform.getAccount(player);

    if (account == null) throw CommandAPI.failWithString("You do not have an account");

    if (getBalance(account) < amount) throw CommandAPI.failWithString("Insufficient funds");

    locale.sendParsedMessage(player, MessageType.TRANSFER_SEND, "sender", player.getName(), "receiver", account.getUsername(), "amount", format(amount), "old-balance", getBalanceFormatted(account), "new-balance", format(getBalance(account) - amount));

    Player targetPlayer = Bukkit.getPlayer(target.getUniqueId());
    if (targetPlayer != null)
      locale.sendParsedMessage(targetPlayer, MessageType.TRANSFER_RECEIVE, "sender", player.getName(), "receiver", account.getUsername(), "amount", format(amount), "old-balance", getBalanceFormatted(target), "new-balance", format(getBalance(target) + amount));

    subtractBalance(account, amount);
    addBalance(target, amount);
  }

  private void executeLeaderboardUpdate(@Nullable CommandSender sender, @Nullable CommandArguments args) {
    updateLeaderboard();
  }

  private void viewLeaderboard(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    List<PlayerAccount> leaderboard = getLeaderboard();

    if (leaderboard == null)
      throw CommandAPI.failWithString("There was an issue retrieving the leaderboard, contact an administrator immediately.");

    int page = (int) args.getOrDefault("page", 1);

    int maxPage = (int) Math.ceil((double) leaderboard.size() / leaderboardPageLength);

    int start = (page - 1) * leaderboardPageLength;
    int end = Math.min(leaderboard.size(), start + leaderboardPageLength);

    locale.sendParsedMessage(sender, MessageType.LEADERBOARD_TITLE, "currency", id, "page", String.valueOf(page), "max-page", String.valueOf(maxPage));
    locale.sendParsedMessage(sender, MessageType.LEADERBOARD_SERVER_TOTAL, "server-total", format(leaderboard.stream().mapToDouble(account -> account.getBalance(id)).sum()));
    for (int i = start; i < end; i++) {
      if (i >= leaderboard.size()) break;
      PlayerAccount account = leaderboard.get(i);
      locale.sendParsedMessage(sender, MessageType.LEADERBOARD_BALANCE_VIEW, "position", String.valueOf(i + 1), "player", account.getUsername(), "balance", getBalanceFormatted(account));
    }
    locale.sendParsedMessage(sender, MessageType.LEADERBOARD_NEXT_PAGE, "command", args.fullInput().split(" ")[0], "page", String.valueOf(page), "next-page", String.valueOf(page == maxPage ? page : page + 1), "max-page", String.valueOf(maxPage));
  }

  private void setPlayerBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    PlayerAccount target = (PlayerAccount) args.get("target");
    Double amount = (Double) args.get("amount");

    if (target == null) throw CommandAPI.failWithString("Player not found");
    if (amount == null) throw CommandAPI.failWithString("Invalid amount");

    locale.sendParsedMessage(sender, MessageType.ADMIN_SET, "player", target.getUsername(), "old-balance", getBalanceFormatted(target), "new-balance", format(amount));

    setBalance(target, amount);
  }

  private void addPlayerBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    PlayerAccount target = (PlayerAccount) args.get("target");
    Double amount = (Double) args.get("amount");

    if (target == null) throw CommandAPI.failWithString("Player not found");
    if (amount == null) throw CommandAPI.failWithString("Invalid amount");

    locale.sendParsedMessage(sender, MessageType.ADMIN_ADD, "player", target.getUsername(), "old-balance", getBalanceFormatted(target), "balance-change", format(amount), "new-balance", format(getBalance(target) + amount));

    addBalance(target, amount);
  }

  private void subtractPlayerBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    PlayerAccount target = (PlayerAccount) args.get("target");
    Double amount = (Double) args.get("amount");

    if (target == null) throw CommandAPI.failWithString("Player not found");
    if (amount == null) throw CommandAPI.failWithString("Invalid amount");

    locale.sendParsedMessage(sender, MessageType.ADMIN_REMOVE, "player", target.getUsername(), "old-balance", getBalanceFormatted(target), "balance-change", format(amount), "new-balance", format(getBalance(target) - amount));

    subtractBalance(target, amount);
  }

  private void resetPlayerBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    PlayerAccount target = (PlayerAccount) args.get("target");

    if (target == null) throw CommandAPI.failWithString("Player not found");

    locale.sendParsedMessage(sender, MessageType.ADMIN_RESET, "player", target.getUsername(), "old-balance", getBalanceFormatted(target));

    resetBalance(target);
  }

  public static class Builder {

    private final String currency;
    private final EconomyPlatform platform;
    private final CurrencyCommand viewCommand = new CurrencyCommand();
    private final CurrencyCommand transferCommand = new CurrencyCommand();
    private final CurrencyCommand leaderboardCommand = new CurrencyCommand();
    private final CurrencyCommand adminCommand = new CurrencyCommand();
    private Locale locale;
    private String singularDisplayName;
    private String pluralDisplayName;
    private double startingBalance = 0.0;
    private Formatters formatter = Formatters.COOL;
    private String formatString = "";
    private int leaderboardPageLength = 10;
    private long leaderboardUpdateInterval = 300;

    public Builder(@NotNull String currency, @NotNull EconomyPlatform platform) {
      Preconditions.checkNotNull(currency, "Currency name cannot be null");
      this.currency = currency.toLowerCase();
      this.platform = platform;
      this.locale = platform.getDefaultLocale();
    }

    public Builder usingYaml(@NotNull File file) {
      return usingYaml(YamlConfiguration.loadConfiguration(file));
    }

    public Builder usingYaml(@NotNull YamlConfiguration config) {
      String capitalCurrency = currency.substring(0, 1).toUpperCase() + currency.substring(1);
      singularDisplayName = config.getString("display-name.singular", capitalCurrency);
      pluralDisplayName = config.getString("display-name.plural", singularDisplayName + "s");
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

    public Builder withPluralDisplayName(@NotNull String plural) {
      this.pluralDisplayName = plural;
      return this;
    }

    public Builder withSingularDisplayName(@NotNull String singular) {
      this.singularDisplayName = singular;
      if (pluralDisplayName == null) pluralDisplayName = singular + "s";
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
      CurrencyFormatter formatter = CurrencyFormatter.of(this.formatter, formatString);
      return new Currency(currency, platform, locale, singularDisplayName, pluralDisplayName, startingBalance, formatter, viewCommand, transferCommand, leaderboardCommand, leaderboardPageLength, leaderboardUpdateInterval, adminCommand);
    }

  }

}
