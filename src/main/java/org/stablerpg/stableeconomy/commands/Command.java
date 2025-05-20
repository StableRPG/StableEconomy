package org.stablerpg.stableeconomy.commands;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;

public class Command {

  public static Command deserialize(ConfigurationSection section) throws DeserializationException {
    Command command = new Command();
    String name = section.getString("name");
    if (name == null || name.isEmpty())
      throw new DeserializationException("Failed to locate name for command " + section.getName());
    command.name(section.getString("name"));
    command.aliases(section.getStringList("aliases").toArray(new String[0]));
    command.permission(section.getString("permission"));
    return command;
  }

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
