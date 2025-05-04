package org.stablerpg.stableeconomy.config;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public interface BasicConfig {

  void load();

  @NotNull Logger getLogger();

}
