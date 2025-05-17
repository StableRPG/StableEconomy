package org.stablerpg.stableeconomy.config;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.logging.Logger;

public interface BasicConfig extends Closeable {

  void load();

  @NotNull Logger getLogger();

  @Override
  default void close() {

  }

}
