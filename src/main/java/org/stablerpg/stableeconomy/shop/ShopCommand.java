package org.stablerpg.stableeconomy.shop;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.configuration.ConfigurationSection;
import org.stablerpg.stableeconomy.commands.Command;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;
import org.stablerpg.stableeconomy.shop.backend.ShopCategory;
import org.stablerpg.stableeconomy.shop.gui.ShopCategoryView;

public class ShopCommand {

  public static ShopCommand deserialize(ShopManager manager, ConfigurationSection section) throws DeserializationException {
    Command command = new Command();

    command.name(section.getName());
    if (section.contains("aliases"))
      command.aliases(section.getStringList("aliases").toArray(new String[0]));
    if (section.contains("permission"))
      command.permission(section.getString("permission"));

    String categoryId = section.getString("category");
    if (categoryId == null)
      throw new DeserializationException("Failed to locate category for command \"%s\"".formatted(section.getName()));
    ShopCategory category = manager.getCategory(categoryId);

    return new ShopCommand(manager, command, category);
  }

  private final ShopManager manager;

  private final Command command;
  private final ShopCategory category;

  public ShopCommand(ShopManager manager, Command command, ShopCategory category) {
    this.manager = manager;
    this.command = command;
    this.category = category;
  }

  public void register() {
    if (!command.canBeCreated())
      manager.getPlatform().getLogger().warning("Failed to register command " + command.name());

    CommandAPICommand command = new CommandAPICommand(this.command.name());

    if (this.command.aliases().length > 0)
      command = command.withAliases(this.command.aliases());

    if (this.command.hasPermission())
      command = command.withPermission(this.command.permission());

    command.executesPlayer((player, args) -> {
        new ShopCategoryView(category).open(player);
      }).register();
  }

  public void unregister() {
    CommandAPI.unregister(command.name());
  }

}
