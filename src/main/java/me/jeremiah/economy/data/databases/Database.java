package me.jeremiah.economy.data.databases;

import com.google.common.base.Preconditions;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.jeremiah.economy.config.BasicConfig;
import me.jeremiah.economy.data.PlayerAccount;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class Database implements Listener, Closeable {

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

  protected final BasicConfig config;

  protected Set<PlayerAccount> entries;
  protected Map<UUID, PlayerAccount> entriesByUUID;
  protected Map<String, PlayerAccount> entriesByUsername;

  private ScheduledTask autoSave;

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
    autoSave = Bukkit.getAsyncScheduler().runAtFixedRate(config.getPlugin(), task -> save(), autoSaveInterval, autoSaveInterval, TimeUnit.SECONDS);
    Bukkit.getPluginManager().registerEvents(this, config.getPlugin());
  }

  protected final void add(PlayerAccount playerAccount) {
    entries.add(playerAccount);
    entriesByUUID.put(playerAccount.getUniqueId(), playerAccount);
    entriesByUsername.put(playerAccount.getUsername(), playerAccount);
  }

  public Set<PlayerAccount> getEntries() {
    return Set.copyOf(entries);
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

  protected abstract void load();

  protected abstract void save();

  public void close() {
    autoSave.cancel();
    autoSave = null;
    save();
    PlayerJoinEvent.getHandlerList().unregister(this);
    entries.clear();
    entriesByUUID.clear();
    entriesByUsername.clear();
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public final void onPlayerJoinEvent(PlayerLoginEvent event) {
    Player player = event.getPlayer();
    getByPlayer(player).ifPresentOrElse(
      playerAccount -> {
        playerAccount.updateUsername(player.getName());
        entriesByUsername.entrySet().removeIf(e -> e.getValue().equals(playerAccount));
        entriesByUsername.put(player.getName(), playerAccount);
      },
      () -> add(new PlayerAccount(player.getUniqueId(), player.getName()))
    );
  }

}
