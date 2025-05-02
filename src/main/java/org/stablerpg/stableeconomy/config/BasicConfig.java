package org.stablerpg.stableeconomy.config;

import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.data.util.DatabaseInfo;

import java.util.logging.Logger;

public interface BasicConfig {

  @NotNull Logger getLogger();

  @NotNull DatabaseInfo getDatabaseInfo();

  void load();

}
