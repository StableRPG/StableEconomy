package me.jeremiah.stableeconomy.config;

import me.jeremiah.stableeconomy.data.util.DatabaseInfo;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public interface BasicConfig {

  @NotNull Logger getLogger();

  @NotNull DatabaseInfo getDatabaseInfo();

  void load();

}
