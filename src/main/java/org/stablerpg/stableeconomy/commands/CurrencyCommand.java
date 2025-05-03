package org.stablerpg.stableeconomy.commands;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public class CurrencyCommand {

  private String name = "";
  private String[] aliases = new String[0];
  private String permission = "";

  public void usingYaml(@Nullable ConfigurationSection config) {
    if (config == null) return;
    name = config.getString("name", "");
    aliases = config.getStringList("aliases").toArray(new String[0]);
    permission = config.getString("permission", "");
  }

  public void name(String name) {
    this.name = name;
  }

  public String name() {
    return name;
  }

  public void aliases(String... aliases) {
    this.aliases = aliases;
  }

  public String[] aliases() {
    return aliases;
  }

  public void permission(String permission) {
    this.permission = permission;
  }

  public String permission() {
    return permission;
  }

  public boolean canBeCreated() {
    return !name.isEmpty();
  }

  public boolean hasPermission() {
    return !permission.isEmpty();
  }

}
