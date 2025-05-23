package org.stablerpg.stableeconomy.currency;

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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.commands.Command;
import org.stablerpg.stableeconomy.commands.arguments.AccountArgument;
import org.stablerpg.stableeconomy.config.currency.CurrencyLocale;
import org.stablerpg.stableeconomy.config.currency.CurrencyMessageType;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;
import org.stablerpg.stableeconomy.currency.formatting.CurrencyFormatter;
import org.stablerpg.stableeconomy.currency.formatting.Formatters;
import org.stablerpg.stableeconomy.data.PlayerAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Currency {

  public static Currency deserialize(EconomyPlatform platform, ConfigurationSection currencySection, ConfigurationSection localeSection) throws DeserializationException {
    String id = currencySection.getString("id");
    if (id == null || id.isEmpty())
      throw new DeserializationException("Failed to locate currency id");

    String capitalCurrency = id.substring(0, 1).toUpperCase() + id.substring(1);

    String singularDisplayName = currencySection.getString("display-name.singular", capitalCurrency);
    String pluralDisplayName = currencySection.getString("display-name.plural", singularDisplayName + "s");

    double startingBalance = currencySection.getDouble("starting-balance", 0.0);

    Command viewCommand = Command.deserialize(currencySection.getConfigurationSection("view-command"));

    Command transferCommand = Command.deserialize(currencySection.getConfigurationSection("transfer-command"));

    ConfigurationSection leaderboardSection = currencySection.getConfigurationSection("leaderboard-command");
    Command leaderboardCommand = Command.deserialize(leaderboardSection);
    int leaderboardPageLength = 10;
    long leaderboardUpdateInterval = 300;
    if (leaderboardSection != null) {
      leaderboardPageLength = leaderboardSection.getInt("page-length", 10);
      leaderboardUpdateInterval = leaderboardSection.getLong("update-interval", 300);
    }

    Command adminCommand = Command.deserialize(currencySection.getConfigurationSection("admin-command"));

    Formatters formatterType = Formatters.fromString(currencySection.getString("formatter", "cool"));
    String formatString = currencySection.getString("format-string", "<amount>");
    CurrencyFormatter formatter = CurrencyFormatter.of(formatterType, formatString);

    CurrencyLocale locale = CurrencyLocale.deserialize(localeSection);

    return new Currency(id, platform, locale, singularDisplayName, pluralDisplayName, startingBalance, formatter, viewCommand, transferCommand, leaderboardCommand, leaderboardPageLength, leaderboardUpdateInterval, adminCommand);
  }

  @Getter
  private final @NotNull String id;
  @Getter
  private final @NotNull EconomyPlatform platform;
  @Getter
  private final @NotNull CurrencyLocale locale;

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

  private Currency(@NotNull String id, @NotNull EconomyPlatform platform, @NotNull CurrencyLocale locale, @NotNull String singularDisplayName, @NotNull String pluralDisplayName, double startingBalance, @NotNull CurrencyFormatter formatter, @NotNull Command viewCommand, @NotNull Command transferCommand, @NotNull Command leaderboardCommand, int leaderboardPageLength, long leaderboardUpdateInterval, @NotNull Command adminCommand) {
    this.id = id;
    this.platform = platform;
    this.locale = locale;
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
      else updatePermission = "stableeconomy.leaderboard.update";

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

  // Balance Actions

  public String format(double amount) {
    return formatter.format(amount);
  }

  public double getBalance(OfflinePlayer player) {
    return platform.getBalance(player, id);
  }

  public double getBalance(UUID uuid) {
    return platform.getBalance(uuid, id);
  }

  public double getBalance(String username) {
    return platform.getBalance(username, id);
  }

  public double getBalance(PlayerAccount account) {
    return account.getBalance(id);
  }

  public String getFormattedBalance(OfflinePlayer player) {
    return format(getBalance(player));
  }

  public String getFormattedBalance(UUID uuid) {
    return format(getBalance(uuid));
  }

  public String getFormattedBalance(String username) {
    return format(getBalance(username));
  }

  public String getFormattedBalance(PlayerAccount account) {
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

  public boolean hasBalance(OfflinePlayer player, double amount) {
    return platform.hasBalance(player, amount, id);
  }

  public boolean hasBalance(UUID uuid, double amount) {
    return platform.hasBalance(uuid, amount, id);
  }

  public boolean hasBalance(String username, double amount) {
    return platform.hasBalance(username, amount, id);
  }

  public boolean hasBalance(PlayerAccount account, double amount) {
    return account.hasBalance(id, amount);
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
    PlayerAccount target = args.getByClass("target", PlayerAccount.class);

    if (target == null)
      throw CommandAPI.failWithString("Failed to retrieve target. Please contact an administrator.");

    locale.sendParsedMessage(sender, CurrencyMessageType.VIEW_OTHER, "player", target.getUsername(), "balance", getFormattedBalance(target));
  }

  private void viewOwnBalance(Player player, CommandArguments args) {
    locale.sendParsedMessage(player, CurrencyMessageType.VIEW_OWN, "balance", getFormattedBalance(player));
  }

  private void transferBalance(Player player, CommandArguments args) throws WrapperCommandSyntaxException {
    PlayerAccount target = args.getByClass("target", PlayerAccount.class);
    Double amount = args.getByClass("amount", Double.class);

    if (target == null)
      throw CommandAPI.failWithString("Failed to retrieve target. Please contact an administrator.");
    if (amount == null)
      throw CommandAPI.failWithString("Failed to retrieve amount. Please contact an administrator.");

    PlayerAccount account = platform.getAccount(player);

    if (account == null)
      throw CommandAPI.failWithString("Failed to retrieve your account. Please contact an administrator.");

    if (getBalance(account) < amount) {
      locale.sendParsedMessage(player, CurrencyMessageType.INSUFFICIENT_BALANCE, "amount", format(amount), "balance", getFormattedBalance(account));
      return;
    }

    locale.sendParsedMessage(player, CurrencyMessageType.TRANSFER_SEND, "sender", player.getName(), "receiver", account.getUsername(), "amount", format(amount), "old-balance", getFormattedBalance(account), "new-balance", format(getBalance(account) - amount));

    Player targetPlayer = Bukkit.getPlayer(target.getUniqueId());
    if (targetPlayer != null)
      locale.sendParsedMessage(targetPlayer, CurrencyMessageType.TRANSFER_RECEIVE, "sender", player.getName(), "receiver", account.getUsername(), "amount", format(amount), "old-balance", getFormattedBalance(target), "new-balance", format(getBalance(target) + amount));

    subtractBalance(account, amount);
    addBalance(target, amount);
  }

  private void executeLeaderboardUpdate(@Nullable CommandSender sender, @Nullable CommandArguments args) {
    updateLeaderboard();
  }

  private void viewLeaderboard(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    List<PlayerAccount> leaderboard = getLeaderboard();

    if (leaderboard == null)
      throw CommandAPI.failWithString("Failed to retrieve the leaderboard. Please contact an administrator.");

    int page = (int) args.getOrDefault("page", 1);

    int maxPage = (int) Math.ceil((double) leaderboard.size() / leaderboardPageLength);

    int start = (page - 1) * leaderboardPageLength;
    int end = Math.min(leaderboard.size(), start + leaderboardPageLength);

    locale.sendParsedMessage(sender, CurrencyMessageType.LEADERBOARD_TITLE, "currency", id, "page", String.valueOf(page), "max-page", String.valueOf(maxPage));
    locale.sendParsedMessage(sender, CurrencyMessageType.LEADERBOARD_SERVER_TOTAL, "server-total", format(leaderboard.stream().mapToDouble(account -> account.getBalance(id)).sum()));
    for (int i = start; i < end; i++) {
      if (i >= leaderboard.size()) break;
      PlayerAccount account = leaderboard.get(i);
      locale.sendParsedMessage(sender, CurrencyMessageType.LEADERBOARD_BALANCE_VIEW, "position", String.valueOf(i + 1), "player", account.getUsername(), "balance", getFormattedBalance(account));
    }
    locale.sendParsedMessage(sender, CurrencyMessageType.LEADERBOARD_NEXT_PAGE, "command", args.fullInput().split(" ")[0], "page", String.valueOf(page), "next-page", String.valueOf(page == maxPage ? page : page + 1), "max-page", String.valueOf(maxPage));
  }

  private void setPlayerBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    PlayerAccount target = args.getByClass("target", PlayerAccount.class);
    Double amount = args.getByClass("amount", Double.class);

    if (target == null)
      throw CommandAPI.failWithString("Failed to retrieve target. Please create an issue at https://github.com/StableLabz/StableEconomy");
    if (amount == null)
      throw CommandAPI.failWithString("Failed to retrieve amount. Please create an issue at https://github.com/StableLabz/StableEconomy");

    locale.sendParsedMessage(sender, CurrencyMessageType.ADMIN_SET, "player", target.getUsername(), "old-balance", getFormattedBalance(target), "new-balance", format(amount));

    setBalance(target, amount);
  }

  private void addPlayerBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    PlayerAccount target = args.getByClass("target", PlayerAccount.class);
    Double amount = args.getByClass("amount", Double.class);

    if (target == null)
      throw CommandAPI.failWithString("Failed to retrieve target. Please create an issue at https://github.com/StableLabz/StableEconomy");
    if (amount == null)
      throw CommandAPI.failWithString("Failed to retrieve amount. Please create an issue at https://github.com/StableLabz/StableEconomy");

    locale.sendParsedMessage(sender, CurrencyMessageType.ADMIN_ADD, "player", target.getUsername(), "old-balance", getFormattedBalance(target), "balance-change", format(amount), "new-balance", format(getBalance(target) + amount));

    addBalance(target, amount);
  }

  private void subtractPlayerBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    PlayerAccount target = args.getByClass("target", PlayerAccount.class);
    Double amount = args.getByClass("amount", Double.class);


    if (target == null)
      throw CommandAPI.failWithString("Failed to retrieve target. Please create an issue at https://github.com/StableLabz/StableEconomy");
    if (amount == null)
      throw CommandAPI.failWithString("Failed to retrieve amount. Please create an issue at https://github.com/StableLabz/StableEconomy");

    locale.sendParsedMessage(sender, CurrencyMessageType.ADMIN_REMOVE, "player", target.getUsername(), "old-balance", getFormattedBalance(target), "balance-change", format(amount), "new-balance", format(getBalance(target) - amount));

    subtractBalance(target, amount);
  }

  private void resetPlayerBalance(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
    PlayerAccount target = args.getByClass("target", PlayerAccount.class);

    if (target == null)
      throw CommandAPI.failWithString("Failed to retrieve target. Please create an issue at https://github.com/StableLabz/StableEconomy");

    locale.sendParsedMessage(sender, CurrencyMessageType.ADMIN_RESET, "player", target.getUsername(), "old-balance", getFormattedBalance(target));

    resetBalance(target);
  }

}
