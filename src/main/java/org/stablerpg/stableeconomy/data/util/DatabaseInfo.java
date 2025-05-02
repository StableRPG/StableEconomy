package org.stablerpg.stableeconomy.data.util;

import lombok.Getter;
import lombok.Setter;

@Getter
public final class DatabaseInfo {

  private final DatabaseType databaseType;

  private final String address;
  private final int port;

  private final String name;

  private final String username;
  private final String password;

  @Setter
  private long autoSaveInterval;

  public DatabaseInfo(String databaseType, String databaseAddress, int databasePort, String name, String username, String password) {
    this.databaseType = DatabaseType.fromString(databaseType);
    this.address = databaseAddress;
    this.port = databasePort;
    this.name = name;
    this.username = username;
    this.password = password;
  }

  public String getUrl() {
    return "%s:%d".formatted(address, port);
  }

  public enum DatabaseType {
    SQLITE,
    H2,
    MYSQL,
    MARIADB,
    POSTGRESQL,
    MONGODB;

    public static DatabaseType fromString(String type) {
      return switch (type.toUpperCase()) {
        case "SQLITE" -> SQLITE;
        case "MYSQL" -> MYSQL;
        case "MARIADB" -> MARIADB;
        case "POSTGRESQL" -> POSTGRESQL;
        case "MONGODB" -> MONGODB;
        default -> H2;
      };
    }

  }

}
