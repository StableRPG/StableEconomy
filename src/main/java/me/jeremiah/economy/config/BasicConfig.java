package me.jeremiah.economy.config;

import me.jeremiah.economy.AbstractEconomyPlugin;
import me.jeremiah.economy.data.util.DatabaseInfo;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public interface BasicConfig {

  @NotNull AbstractEconomyPlugin getPlugin();

  @NotNull Logger getLogger();

  @NotNull DatabaseInfo getDatabaseInfo();

  void load();

}
