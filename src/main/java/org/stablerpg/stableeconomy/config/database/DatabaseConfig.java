package org.stablerpg.stableeconomy.config.database;

import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.config.BasicConfig;
import org.stablerpg.stableeconomy.data.util.DatabaseInfo;

public interface DatabaseConfig extends BasicConfig {

  @NotNull DatabaseInfo getDatabaseInfo();

}
