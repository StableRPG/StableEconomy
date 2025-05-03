package org.stablerpg.stableeconomy.config;

import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.data.util.DatabaseInfo;

import java.util.logging.Logger;

public interface BasicConfig {

  void load();

  @NotNull DatabaseInfo getDatabaseInfo();

  @NotNull Logger getLogger();

}
