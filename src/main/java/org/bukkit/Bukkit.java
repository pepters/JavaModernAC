package org.bukkit;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class Bukkit {
    private static final BukkitScheduler scheduler = new BukkitScheduler();

    public static BukkitScheduler getScheduler() { return scheduler; }

    public static Player getPlayer(UUID uuid) { return null; }

    public static OfflinePlayer getOfflinePlayer(UUID uuid) { return new OfflinePlayer(uuid); }

    public static OfflinePlayer getOfflinePlayer(String name) {
        return new OfflinePlayer(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
    }

    public static void dispatchCommand(CommandSender sender, String command) {}

    public static CommandSender getConsoleSender() { return null; }

    public static Collection<? extends Player> getOnlinePlayers() { return Collections.emptyList(); }

    public static double[] getTPS() { return new double[] {20.0, 20.0, 20.0}; }
}
