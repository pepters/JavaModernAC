package com.modernac.engine;

import com.modernac.ModernACPlugin;
import com.modernac.checks.Check;
import com.modernac.manager.PunishmentTier;
import com.modernac.manager.MitigationManager;
import com.modernac.config.ConfigManager;
import com.modernac.engine.AlertEngine.AlertDetail;
import org.bukkit.Bukkit;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aggregates evidence from independent checks and applies policy logic.
 */
public class DetectionEngine {
    private final ModernACPlugin plugin;
    private final Map<UUID, PlayerRecord> records = new ConcurrentHashMap<>();

    public DetectionEngine(ModernACPlugin plugin) {
        this.plugin = plugin;
    }

    public void record(Check check, int vl, boolean punishable, boolean experimental) {
        if (experimental && !plugin.getConfigManager().isExperimentalDetections()) {
            return;
        }
        double evidence = Math.min(1.0, vl / 100.0);
        DetectionResult result = new DetectionResult("LEGACY", evidence, Window.SHORT, true, true, false);
        record(check, result);
    }

    public void record(Check check, DetectionResult result) {
        if (check.isExperimental() && !plugin.getConfigManager().isExperimentalDetections()) {
            return;
        }
        UUID uuid = check.getUuid();
        if (plugin.isExempt(uuid)) {
            return;
        }
        PlayerRecord record = records.computeIfAbsent(uuid, u -> new PlayerRecord());
        FamilyRecord fam = record.families.computeIfAbsent(result.getFamily(), f -> new FamilyRecord());
        double prev = fam.windowScores.getOrDefault(result.getWindow(), 0.0);
        fam.windowScores.put(result.getWindow(), Math.max(prev, result.getEvidenceScore()));
        fam.latencyOK &= result.isLatencyOK();
        fam.stabilityOK &= result.isStabilityOK();
        ConfigManager cfg = plugin.getConfigManager();
        PunishmentTier tier = cfg.getCheckTier(check.getName());
        if (tier.ordinal() < fam.tier.ordinal()) {
            fam.tier = tier;
        }
        evaluate(uuid, record);
        int ping = Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getPing() : 0;
        double tps = Bukkit.getTPS()[0];
        AlertDetail detail = new AlertDetail(result.getFamily(), result.getWindow().name(), result.getEvidenceScore(), ping, tps);
        plugin.getAlertEngine().queueAlert(uuid, detail, false);
    }

    private void evaluate(UUID uuid, PlayerRecord record) {
        ConfigManager cfg = plugin.getConfigManager();
        int requiredFamilies = cfg.getMinFamiliesForBan();
        boolean requireMultiWindow = cfg.isMultiWindowConfirmationRequired();
        int familyCount = 0;
        int windowCount = 0;
        double total = 0.0;
        PunishmentTier highest = null;
        for (FamilyRecord fam : record.families.values()) {
            double famScore = fam.windowScores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            if (fam.latencyOK && fam.stabilityOK && famScore >= 0.90) {
                familyCount++;
                windowCount += (int) fam.windowScores.values().stream().filter(v -> v >= 0.90).count();
                if (highest == null || fam.tier.ordinal() < highest.ordinal()) {
                    highest = fam.tier;
                }
                total += famScore;
            }
        }
        double avg = familyCount > 0 ? total / familyCount : 0.0;
        MitigationManager mit = plugin.getMitigationManager();
        double maxReduction = cfg.getMaxDamageReduction();
        int reduction = (int) Math.round(avg * maxReduction * 100);
        mit.mitigate(uuid, reduction);

        if (familyCount >= requiredFamilies && (!requireMultiWindow || windowCount >= 2) && highest != null && highest != PunishmentTier.EXPERIMENTAL) {
            record.currentTier = highest;
            plugin.getPunishmentManager().schedule(uuid, highest);
        } else {
            record.currentTier = null;
            plugin.getPunishmentManager().cancel(uuid);
        }
    }

    public boolean isPunishable(UUID uuid, PunishmentTier tier) {
        PlayerRecord record = records.get(uuid);
        if (record == null || record.currentTier == null) return false;
        return record.currentTier.ordinal() <= tier.ordinal();
    }

    public void reset(UUID uuid) {
        records.remove(uuid);
        plugin.getPunishmentManager().cancel(uuid);
    }

    public void shutdown() {
        for (UUID uuid : records.keySet()) {
            plugin.getPunishmentManager().cancel(uuid);
        }
        records.clear();
    }

    private static class PlayerRecord {
        final Map<String, FamilyRecord> families = new ConcurrentHashMap<>();
        PunishmentTier currentTier;
    }

    private static class FamilyRecord {
        final Map<Window, Double> windowScores = new EnumMap<>(Window.class);
        boolean latencyOK = true;
        boolean stabilityOK = true;
        PunishmentTier tier = PunishmentTier.MEDIUM;
    }
}
