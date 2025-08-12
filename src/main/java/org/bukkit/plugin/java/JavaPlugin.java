package org.bukkit.plugin.java;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * Simplified stand-in for Bukkit's {@code JavaPlugin}. It implements the
 * {@link Plugin} marker so code depending on the interface can compile.
 */
public class JavaPlugin implements Plugin {
    private final FileConfiguration config = new FileConfiguration();

    public void saveDefaultConfig() {}

    public void saveResource(String name, boolean replace) {}

    public FileConfiguration getConfig() { return config; }

    public File getDataFolder() { return new File("."); }

    public Server getServer() { return null; }

    public Logger getLogger() { return Logger.getLogger("JavaPlugin"); }

    public void onLoad() {}
    public void onEnable() {}

    public void onDisable() {}
}
