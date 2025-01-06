package me.jeremiah.economy.config;

import me.jeremiah.economy.data.util.DatabaseInfo;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public interface BasicConfig {

  @NotNull Logger getLogger();

  @NotNull DatabaseInfo getDatabaseInfo();

  void load();

}
