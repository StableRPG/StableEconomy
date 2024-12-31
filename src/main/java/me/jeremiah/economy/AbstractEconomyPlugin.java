package me.jeremiah.economy;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractEconomyPlugin extends JavaPlugin {

  public abstract EconomyPlatform getEconomyPlatform();

}
