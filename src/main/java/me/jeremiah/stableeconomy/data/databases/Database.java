package me.jeremiah.stableeconomy.data.databases;

import com.google.common.base.Preconditions;
import me.jeremiah.stableeconomy.EconomyPlatform;
import me.jeremiah.stableeconomy.config.BasicConfig;
import me.jeremiah.stableeconomy.data.PlayerAccount;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Database implements Closeable {

  // TODO: Possibly refactor this class to use a SingleThreadExecutor and CompletableFuture for all methods.

  public static @NotNull Database of(@NotNull EconomyPlatform platform) {
    return switch (platform.getConfig().getDatabaseInfo().getDatabaseType()) {
      case SQLITE -> new SQLite(platform);
      case H2 -> new H2(platform);
      case MYSQL, MARIADB -> new MariaDB(platform);
      case POSTGRESQL -> new PostgreSQL(platform);
      case MONGODB -> new MongoDB(platform);
    };
  }

  private final EconomyPlatform platform;

  private ScheduledFuture<?> autoSaveTask;

  protected Set<PlayerAccount> entries;
  protected Map<UUID, PlayerAccount> entriesByUUID;
  protected Map<String, PlayerAccount> entriesByUsername;

  protected Database(@NotNull EconomyPlatform platform) {
    this.platform = platform;
  }

  public final EconomyPlatform getPlatform() {
    return platform;
  }

  public final BasicConfig getConfig() {
    return platform.getConfig();
  }

  protected final ScheduledExecutorService getScheduler() {
    return platform.getScheduler();
  }

  protected int lookupEntryCount() {
    return Bukkit.getOfflinePlayers().length;
  }

  protected void setup() {
    int initialCapacity = lookupEntryCount() * 2;
    entries = ConcurrentHashMap.newKeySet(initialCapacity);
    entriesByUUID = new ConcurrentHashMap<>(initialCapacity);
    entriesByUsername = new ConcurrentHashMap<>(initialCapacity);
    load();
    long autoSaveInterval = getConfig().getDatabaseInfo().getAutoSaveInterval();
    autoSaveTask = getScheduler().scheduleAtFixedRate(this::save, autoSaveInterval, autoSaveInterval, TimeUnit.SECONDS);
  }

  public final void add(PlayerAccount playerAccount) {
    entries.add(playerAccount);
    entriesByUUID.put(playerAccount.getUniqueId(), playerAccount);
    entriesByUsername.put(playerAccount.getUsername(), playerAccount);
  }

  public final <R> R queryByPlayer(@NotNull OfflinePlayer player, @NotNull Function<PlayerAccount, R> query) {
    return getByPlayer(player).map(query).orElse(null);
  }

  public final <R> R queryByUUID(@NotNull UUID uniqueId, @NotNull Function<PlayerAccount, R> query) {
    return getByUUID(uniqueId).map(query).orElse(null);
  }

  public final <R> R queryByUsername(@NotNull String username, @NotNull Function<PlayerAccount, R> query) {
    return getByUsername(username).map(query).orElse(null);
  }

  public final void updateByPlayer(@NotNull OfflinePlayer player, Consumer<PlayerAccount> consumer) {
    PlayerAccount account = getByPlayer(player).orElseThrow(() -> new IllegalStateException("Player account not found"));
    consumer.accept(account);
  }

  public final CompletableFuture<Void> updateByPlayerAsync(@NotNull OfflinePlayer player, Consumer<PlayerAccount> consumer) {
    return CompletableFuture.runAsync(() -> updateByPlayer(player, consumer), getScheduler());
  }

  public final void updateByUUID(@NotNull UUID uniqueId, Consumer<PlayerAccount> consumer) {
    PlayerAccount account = getByUUID(uniqueId).orElseThrow(() -> new IllegalStateException("Player account not found"));
    consumer.accept(account);
  }

  public final CompletableFuture<Void> updateByUUIDAsync(@NotNull UUID uniqueId, Consumer<PlayerAccount> consumer) {
    return CompletableFuture.runAsync(() -> updateByUUID(uniqueId, consumer), getScheduler());
  }

  public final void updateByUsername(@NotNull String username, Consumer<PlayerAccount> consumer) {
    PlayerAccount account = getByUsername(username).orElseThrow(() -> new IllegalStateException("Player account not found"));
    consumer.accept(account);
  }

  public final CompletableFuture<Void> updateByUsernameAsync(@NotNull String username, Consumer<PlayerAccount> consumer) {
    return CompletableFuture.runAsync(() -> updateByUsername(username, consumer), getScheduler());
  }

  public final List<PlayerAccount> sortedByBalance(String currency) {
    ArrayList<PlayerAccount> sorted = new ArrayList<>(entries);
    sorted.sort(Comparator.comparing(playerAccount -> playerAccount.getBalanceEntry(currency), Comparator.reverseOrder()));
    return sorted;
  }

  public final Optional<PlayerAccount> getByPlayer(@NotNull OfflinePlayer player) {
    Preconditions.checkNotNull(player, "Player cannot be null");
    return getByUUID(player.getUniqueId());
  }

  public final Optional<PlayerAccount> getByUUID(@NotNull UUID uniqueId) {
    Preconditions.checkNotNull(uniqueId, "UUID cannot be null");
    return Optional.ofNullable(entriesByUUID.get(uniqueId));
  }

  public final Optional<PlayerAccount> getByUsername(@NotNull String username) {
    Preconditions.checkNotNull(username, "Username cannot be null");
    return Optional.ofNullable(entriesByUsername.get(username));
  }

  public final void createOrUpdateAccount(@NotNull UUID uniqueId, @NotNull String username) {
    Preconditions.checkNotNull(uniqueId, "UUID cannot be null");
    Preconditions.checkNotNull(username, "Username cannot be null");
    getByUUID(uniqueId).ifPresentOrElse(
      playerAccount -> {
        playerAccount.updateUsername(username);
        entriesByUsername.entrySet().removeIf(e -> e.getValue().equals(playerAccount));
        entriesByUsername.put(username, playerAccount);
      },
      () -> add(new PlayerAccount(platform, uniqueId, username))
    );
  }

  protected abstract void load();

  protected abstract void save();

  public void close() {
    autoSaveTask.cancel(false);
    save();
    entries.clear();
    entriesByUUID.clear();
    entriesByUsername.clear();
  }

}
