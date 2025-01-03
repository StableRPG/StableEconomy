package me.jeremiah.economy.data.databases;

import com.google.common.base.Preconditions;
import me.jeremiah.economy.config.BasicConfig;
import me.jeremiah.economy.data.PlayerAccount;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

public abstract class Database implements Closeable {

  // TODO: Possibly refactor this class to use a SingleThreadExecutor and CompletableFuture for all methods.

  public static @NotNull Database of(BasicConfig config) {
    return switch (config.getDatabaseInfo().getDatabaseType()) {
      case SQLITE -> new SQLite(config);
      case H2 -> new H2(config);
      case MYSQL -> new MySQL(config);
      case MARIADB -> new MariaDB(config);
      case POSTGRESQL -> new PostgreSQL(config);
      case MONGODB -> new MongoDB(config);
    };
  }

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  protected final BasicConfig config;

  protected Set<PlayerAccount> entries;
  protected Map<UUID, PlayerAccount> entriesByUUID;
  protected Map<String, PlayerAccount> entriesByUsername;

  protected Database(@NotNull BasicConfig config) {
    this.config = config;
  }

  protected int lookupEntryCount() {
    return Bukkit.getOfflinePlayers().length;
  }

  protected final void setup() {
    int initialCapacity = lookupEntryCount() * 2;
    entries = ConcurrentHashMap.newKeySet(initialCapacity);
    entriesByUUID = new ConcurrentHashMap<>(initialCapacity);
    entriesByUsername = new ConcurrentHashMap<>(initialCapacity);
    load();
    long autoSaveInterval = config.getDatabaseInfo().getAutoSaveInterval();
    scheduler.scheduleAtFixedRate(this::save, autoSaveInterval, autoSaveInterval, TimeUnit.SECONDS);
  }

  public final void add(PlayerAccount playerAccount) {
    entries.add(playerAccount);
    entriesByUUID.put(playerAccount.getUniqueId(), playerAccount);
    entriesByUsername.put(playerAccount.getUsername(), playerAccount);
  }

  public Set<PlayerAccount> getEntries() {
    return Set.copyOf(entries);
  }

  public boolean updateByPlayer(@NotNull OfflinePlayer player, Consumer<PlayerAccount> consumer) {
      Optional<PlayerAccount> optional = getByPlayer(player);
      if (optional.isPresent()) {
        consumer.accept(optional.get());
        return true;
      }
      return false;
  }

  public boolean updateByUUID(@NotNull UUID uniqueId, Consumer<PlayerAccount> consumer) {
      Optional<PlayerAccount> optional = getByUUID(uniqueId);
      if (optional.isPresent()) {
        consumer.accept(optional.get());
        return true;
      }
      return false;
  }

  public boolean updateByUsername(@NotNull String username, Consumer<PlayerAccount> consumer) {
    Optional<PlayerAccount> optional = getByUsername(username);
    if (optional.isPresent()) {
      consumer.accept(optional.get());
      return true;
    }
    return false;
  }

  public CompletableFuture<Boolean> updateByPlayerAsync(@NotNull OfflinePlayer player, Consumer<PlayerAccount> consumer) {
    return CompletableFuture.supplyAsync(() -> updateByPlayer(player, consumer), scheduler);
  }

  public CompletableFuture<Boolean> updateByUUIDAsync(@NotNull UUID uniqueId, Consumer<PlayerAccount> consumer) {
    return CompletableFuture.supplyAsync(() -> updateByUUID(uniqueId, consumer), scheduler);
  }

  public CompletableFuture<Boolean> updateByUsernameAsync(@NotNull String username, Consumer<PlayerAccount> consumer) {
    return CompletableFuture.supplyAsync(() -> updateByUsername(username, consumer), scheduler);
  }

  public Optional<PlayerAccount> getByPlayer(@NotNull OfflinePlayer player) {
    Preconditions.checkNotNull(player, "Player cannot be null");
    return getByUUID(player.getUniqueId());
  }

  public Optional<PlayerAccount> getByUUID(@NotNull UUID uniqueId) {
    Preconditions.checkNotNull(uniqueId, "UUID cannot be null");
    return Optional.ofNullable(entriesByUUID.get(uniqueId));
  }

  public Optional<PlayerAccount> getByUsername(@NotNull String username) {
    Preconditions.checkNotNull(username, "Username cannot be null");
    return Optional.ofNullable(entriesByUsername.get(username));
  }

  public void createOrUpdateAccount(@NotNull UUID uniqueId, @NotNull String username) {
    Preconditions.checkNotNull(uniqueId, "UUID cannot be null");
    Preconditions.checkNotNull(username, "Username cannot be null");
    getByUUID(uniqueId).ifPresentOrElse(
      playerAccount -> {
        playerAccount.updateUsername(username);
        entriesByUsername.entrySet().removeIf(e -> e.getValue().equals(playerAccount));
        entriesByUsername.put(username, playerAccount);
      },
      () -> add(new PlayerAccount(uniqueId, username))
    );
  }

  protected abstract void load();

  protected abstract void save();

  public void close() {
    scheduler.shutdown();

    try {
      if (!scheduler.awaitTermination(10, TimeUnit.SECONDS))
        scheduler.shutdownNow();
    } catch (InterruptedException exception) {
      scheduler.shutdownNow();
    }

    save();
    entries.clear();
    entriesByUUID.clear();
    entriesByUsername.clear();
  }

}
