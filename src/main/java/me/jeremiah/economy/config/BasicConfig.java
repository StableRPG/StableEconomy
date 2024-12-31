package me.jeremiah.economy.config;

import me.jeremiah.economy.AbstractEconomyPlugin;
import me.jeremiah.economy.storage.DatabaseInfo;
import org.jetbrains.annotations.NotNull;

public interface BasicConfig {

  @NotNull AbstractEconomyPlugin getPlugin();

  @NotNull DatabaseInfo getDatabaseInfo();

  void load();

}
