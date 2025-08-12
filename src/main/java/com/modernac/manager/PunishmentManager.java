package com.modernac.manager;

import com.modernac.ModernACPlugin;
import org.bukkit.Bukkit;

import java.util.Random;
import java.util.UUID;

public class PunishmentManager {
    private final ModernACPlugin plugin;
    private final Random random = new Random();

    public PunishmentManager(ModernACPlugin plugin) {
        this.plugin = plugin;
    }

    public void punish(UUID uuid) {
        int min = plugin.getConfigManager().getPunishmentDelayMin();
        int max = plugin.getConfigManager().getPunishmentDelayMax();
        long delay = 20L * 60L * (min + random.nextInt(Math.max(1, max - min + 1)));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String command = plugin.getConfigManager().getBanCommand().replace("%player%", Bukkit.getOfflinePlayer(uuid).getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }, delay);
    }
}
