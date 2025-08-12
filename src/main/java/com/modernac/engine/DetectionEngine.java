package com.modernac.engine;

import com.modernac.ModernACPlugin;
import com.modernac.checks.Check;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aggregates evidence from independent checks and turns it into a confidence score.
 * Heavy math is offloaded to async tasks; only small state mutation happens on the calling thread.
 */
public class DetectionEngine {
    private static final double BAN_THRESHOLD = 1.0; // 100% confidence

    private final ModernACPlugin plugin;
    private final Map<UUID, PlayerRecord> records = new ConcurrentHashMap<>();
    private BukkitTask decayTask;

    public DetectionEngine(ModernACPlugin plugin) {
        this.plugin = plugin;
        scheduleDecay();
    }

    /**
     * Add evidence for a player.
     *
     * @param check       The originating check
     * @param vl          violation level contribution (0-100)
     * @param punishable  whether the evidence may lead to punishments
     * @param experimental whether the check is experimental
     */
    public void record(Check check, int vl, boolean punishable, boolean experimental) {
        if (experimental && !plugin.getConfigManager().isExperimentalDetections()) {
            return; // ignore if experimental detections disabled
        }

        UUID uuid = check.getUuid();
        PlayerRecord record = records.computeIfAbsent(uuid, k -> new PlayerRecord());

        // map violation level into [0,1] confidence contribution
        double evidence = Math.min(1.0, vl / 100.0);
        record.confidence = Math.min(1.0, record.confidence + evidence);

        int totalVl = check.addVl(vl);
        plugin.getDebugLogger().log(uuid + " failed " + check.getName() + " VL=" + totalVl);

        plugin.getAlertManager().alert(uuid, check.getName(), totalVl);

        double maxReduction = plugin.getConfigManager().getMaxDamageReduction();
        int reduction = (int) Math.round(record.confidence * maxReduction * 100);
        plugin.getMitigationManager().mitigate(uuid, reduction);

        if (punishable && !experimental && record.confidence >= BAN_THRESHOLD) {
            plugin.getPunishmentManager().schedule(uuid);
        }
    }

    public boolean isConfident(UUID uuid) {
        return records.getOrDefault(uuid, PlayerRecord.EMPTY).confidence >= BAN_THRESHOLD;
    }

    public void reset(UUID uuid) {
        records.remove(uuid);
        plugin.getPunishmentManager().cancel(uuid);
    }

    public void shutdown() {
        if (decayTask != null) {
            decayTask.cancel();
        }
    }

    private void scheduleDecay() {
        decayTask = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            decay();
            scheduleDecay();
        }, 20L);
    }

    private void decay() {
        double decayAmount = 0.02; // lose 2% per second
        records.forEach((uuid, record) -> {
            record.confidence = Math.max(0.0, record.confidence - decayAmount);
            if (record.confidence < BAN_THRESHOLD) {
                plugin.getPunishmentManager().cancel(uuid);
            }
        });
    }

    private static class PlayerRecord {
        static final PlayerRecord EMPTY = new PlayerRecord();
        double confidence = 0.0;
    }
}

