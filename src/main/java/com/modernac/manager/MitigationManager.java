package com.modernac.manager;

import com.modernac.ModernACPlugin;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MitigationManager {
    private final ModernACPlugin plugin;
    private final Map<UUID, Integer> mitigated = new ConcurrentHashMap<>();
    private final Map<UUID, Double> baseValues = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> applyTasks = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> removeTasks = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public MitigationManager(ModernACPlugin plugin) {
        this.plugin = plugin;
    }

    public void mitigate(UUID uuid, int level) {
        mitigated.put(uuid, level);

        // schedule application after random delay (5-15s) on main thread
        Integer oldApply = applyTasks.remove(uuid);
        if (oldApply != null) Bukkit.getScheduler().cancelTask(oldApply);
        long applyDelay = 20L * (5 + random.nextInt(11));
        int applyTask = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> apply(uuid), applyDelay);
        applyTasks.put(uuid, applyTask);

        // schedule removal after configured duration and restore base value
        Integer oldRemove = removeTasks.remove(uuid);
        if (oldRemove != null) Bukkit.getScheduler().cancelTask(oldRemove);
        long duration = 20L * plugin.getConfigManager().getMitigationDurationSeconds();
        int removeTask = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            mitigated.remove(uuid);
            Double base = baseValues.remove(uuid);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
                if (attr != null && base != null) {
                    attr.setBaseValue(base);
                }
            }
            removeTasks.remove(uuid);
        }, duration);
        removeTasks.put(uuid, removeTask);
    }

    private void apply(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attr == null) return;
        baseValues.computeIfAbsent(uuid, k -> attr.getBaseValue());
        double base = baseValues.get(uuid);
        int level = mitigated.getOrDefault(uuid, 0);
        double modifier = base * (1 - level / 100.0);
        attr.setBaseValue(modifier);
    }
}
