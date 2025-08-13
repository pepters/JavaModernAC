package com.modernac.manager;

import com.modernac.ModernACPlugin;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MitigationManager {
    private final ModernACPlugin plugin;
    private final Map<UUID, Integer> mitigated = new ConcurrentHashMap<>();
    private final Map<UUID, Double> baseValues = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> applyTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> removeTasks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> removeExpires = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public MitigationManager(ModernACPlugin plugin) {
        this.plugin = plugin;
    }

    public void mitigate(UUID uuid, int level) {
        int clamped = Math.max(0, Math.min(100, level));
        mitigated.put(uuid, clamped);

        // schedule application after random delay on main thread
        BukkitTask oldApply = applyTasks.remove(uuid);
        if (oldApply != null) oldApply.cancel();
        int applyMin = plugin.getConfigManager().getMitigationApplyDelayMin();
        int applyMax = plugin.getConfigManager().getMitigationApplyDelayMax();
        long applyDelay = 20L * (applyMin + random.nextInt(Math.max(1, applyMax - applyMin + 1)));
        BukkitTask applyTask = Bukkit.getScheduler().runTaskLater(plugin, () -> apply(uuid), applyDelay);
        applyTasks.put(uuid, applyTask);

        // schedule removal after configured duration range and restore base value
        BukkitTask oldRemove = removeTasks.remove(uuid);
        if (oldRemove != null) oldRemove.cancel();
        int durMin = plugin.getConfigManager().getMitigationDurationMin();
        int durMax = plugin.getConfigManager().getMitigationDurationMax();
        long duration = 20L * (durMin + random.nextInt(Math.max(1, durMax - durMin + 1)));
        long removeAt = System.currentTimeMillis() + duration * 50L;
        BukkitTask removeTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
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
            removeExpires.remove(uuid);
        }, duration);
        removeTasks.put(uuid, removeTask);
        removeExpires.put(uuid, removeAt);
    }

    private void apply(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> apply(uuid), 20L);
            return;
        }
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attr == null) {
            return;
        }
        baseValues.computeIfAbsent(uuid, k -> attr.getBaseValue());
        double base = baseValues.get(uuid);
        if (!Double.isFinite(base)) {
            base = 1.0;
        }
        int level = Math.max(0, Math.min(100, mitigated.getOrDefault(uuid, 0)));
        double max = plugin.getConfigManager().getMaxDamageReduction();
        if (!Double.isFinite(max) || max < 0) max = 0;
        if (max > 1) max = 1;
        double modifier = base * (1 - (level / 100.0) * max);
        attr.setBaseValue(modifier);
    }

    public void reload() {
        for (BukkitTask task : applyTasks.values()) {
            task.cancel();
        }
        applyTasks.clear();
        for (BukkitTask task : removeTasks.values()) {
            task.cancel();
        }
        removeTasks.clear();
        removeExpires.clear();
        for (Map.Entry<UUID, Integer> entry : mitigated.entrySet()) {
            mitigate(entry.getKey(), entry.getValue());
        }
    }

    public long getRemaining(UUID uuid) {
        Long exp = removeExpires.get(uuid);
        if (exp == null) return 0L;
        long diff = exp - System.currentTimeMillis();
        return Math.max(0L, diff);
    }
}
