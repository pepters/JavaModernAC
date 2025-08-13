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
        int ping = 0;
        double tps = 20.0;
        if (Bukkit.getPlayer(uuid) != null) {
            ping = Bukkit.getPlayer(uuid).getPing();
        }
        double[] tpsArr = Bukkit.getTPS();
        if (tpsArr.length > 0 && Double.isFinite(tpsArr[0])) {
            tps = tpsArr[0];
        }
        boolean soft = evaluate(uuid, record, ping, tps);
        AlertDetail detail = new AlertDetail(result.getFamily(), result.getWindow().name(), result.getEvidenceScore(), ping, tps);
        plugin.getAlertEngine().queueAlert(uuid, detail, soft);
    }

    private boolean evaluate(UUID uuid, PlayerRecord record, int ping, double tps) {
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
        if (!Double.isFinite(maxReduction) || maxReduction < 0) maxReduction = 0;
        if (maxReduction > 1) maxReduction = 1;
        int reduction = (int) Math.round(avg * maxReduction * 100);
        mit.mitigate(uuid, reduction);

        boolean soft = ping > cfg.getUnstableConnectionLimit() || tps < cfg.getTpsSoftGuard();
        if (soft && highest != null) {
            if (highest == PunishmentTier.CRITICAL) {
                highest = PunishmentTier.HIGH;
            } else if (highest == PunishmentTier.HIGH) {
                highest = PunishmentTier.MEDIUM;
            }
        }

        if (familyCount >= requiredFamilies && (!requireMultiWindow || windowCount >= 2) && highest != null && highest != PunishmentTier.EXPERIMENTAL) {
            record.currentTier = highest;
            plugin.getPunishmentManager().schedule(uuid, highest);
        } else {
            record.currentTier = null;
            plugin.getPunishmentManager().cancel(uuid);
        }
        return soft;
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

    public void reload() {
        for (UUID uuid : records.keySet()) {
            plugin.getPunishmentManager().cancel(uuid);
        }
        records.clear();
    }

    public DetectionSummary getSummary(UUID uuid) {
        PlayerRecord record = records.get(uuid);
        if (record == null) return null;
        double shortMax = 0.0;
        double longMax = 0.0;
        double veryLongMax = 0.0;
        boolean latencyOK = true;
        boolean stabilityOK = true;
        for (FamilyRecord fam : record.families.values()) {
            shortMax = Math.max(shortMax, fam.windowScores.getOrDefault(Window.SHORT, 0.0));
            longMax = Math.max(longMax, fam.windowScores.getOrDefault(Window.LONG, 0.0));
            veryLongMax = Math.max(veryLongMax, fam.windowScores.getOrDefault(Window.VERY_LONG, 0.0));
            latencyOK &= fam.latencyOK;
            stabilityOK &= fam.stabilityOK;
        }
        return new DetectionSummary(latencyOK, stabilityOK, shortMax, longMax, veryLongMax);
    }

    public static class DetectionSummary {
        public final boolean latencyOK;
        public final boolean stabilityOK;
        public final double shortWindow;
        public final double longWindow;
        public final double veryLongWindow;
        public DetectionSummary(boolean latencyOK, boolean stabilityOK, double shortWindow, double longWindow, double veryLongWindow) {
            this.latencyOK = latencyOK;
            this.stabilityOK = stabilityOK;
            this.shortWindow = shortWindow;
            this.longWindow = longWindow;
            this.veryLongWindow = veryLongWindow;
        }
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
