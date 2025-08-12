package com.modernac.manager;

import com.modernac.ModernACPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import com.modernac.manager.PunishmentTier;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles delayed punishments. Punishments are only executed if the confidence
 * at execution time is still above the threshold.
 */
public class PunishmentManager {
    private final ModernACPlugin plugin;
    private final Random random = new Random();
    private final Map<UUID, BukkitTask> tasks = new ConcurrentHashMap<>();

    public PunishmentManager(ModernACPlugin plugin) {
        this.plugin = plugin;
    }

    public void schedule(UUID uuid, PunishmentTier tier) {
        if (tasks.containsKey(uuid)) return;
        int[] range = plugin.getConfigManager().getTierDelaySeconds(tier);
        int min = range[0];
        int max = range[1];
        long delay = 20L * (min + random.nextInt(Math.max(1, max - min + 1)));
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            tasks.remove(uuid);
            if (plugin.getDetectionEngine().isPunishable(uuid, tier)) {
                String command = plugin.getConfigManager().getBanCommand()
                        .replace("{player}", Bukkit.getOfflinePlayer(uuid).getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }, delay);
        tasks.put(uuid, task);
    }

    public void cancel(UUID uuid) {
        BukkitTask task = tasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }
}

