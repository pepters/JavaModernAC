package com.modernac.manager;

import com.modernac.ModernACPlugin;
import org.bukkit.Bukkit;

import java.util.Random;
import java.util.UUID;

public class AlertManager {
    private final ModernACPlugin plugin;
    private final Random random = new Random();

    public AlertManager(ModernACPlugin plugin) {
        this.plugin = plugin;
    }

    public void alert(UUID uuid, String check, int vl) {
        int min = plugin.getConfigManager().getAlertDelayMin();
        int max = plugin.getConfigManager().getAlertDelayMax();
        long delay = 20L * (min + random.nextInt(Math.max(1, max - min + 1)));
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            String message = plugin.getMessageManager().getMessage("alert")
                    .replace("{player}", Bukkit.getOfflinePlayer(uuid).getName())
                    .replace("{check}", check)
                    .replace("{vl}", Integer.toString(vl));
            String permission = plugin.getConfigManager().getAlertPermission();
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission(permission))
                    .forEach(p -> p.sendMessage(message));
        }, delay);
    }
}
