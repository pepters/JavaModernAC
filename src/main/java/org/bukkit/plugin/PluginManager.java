package org.bukkit.plugin;

import org.bukkit.event.Listener;

/**
 * Minimal representation of Bukkit's {@code PluginManager} used for
 * compilation. The runtime server provides the real implementation.
 */
public interface PluginManager {
    /**
     * Register an event listener with the given plugin.
     */
    void registerEvents(Listener listener, Plugin plugin);
}
