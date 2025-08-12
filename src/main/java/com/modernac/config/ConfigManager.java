package com.modernac.config;

import com.modernac.ModernACPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import com.modernac.manager.PunishmentTier;

public class ConfigManager {
    private final FileConfiguration config;

    public ConfigManager(ModernACPlugin plugin) {
        this.config = plugin.getConfig();
    }

    public String getKickCommand() {
        return config.getString("commands.kick", "kick {player} Unfair combat advantage");
    }

    public String getBanCommand() {
        return config.getString("commands.ban", "ban {player} 7d Unfair combat advantage");
    }

    public int getUnstableConnectionLimit() {
        return config.getInt("latency.unstable_connection_latency_limit", 1750);
    }

    public double getTpsSoftGuard() {
        String raw = config.getString("latency.tps_soft_guard", null);
        return raw != null ? Double.parseDouble(raw) : 18.0D;
    }

    public boolean isExperimentalDetections() {
        return config.getBoolean("checks.experimental_detections", true);
    }

    public CombatTolerance getCombatTolerance() {
        String value = config.getString("checks.combat_tolerance", "NORMAL").toUpperCase();
        try {
            return CombatTolerance.valueOf(value);
        } catch (IllegalArgumentException e) {
            return CombatTolerance.NORMAL;
        }
    }

    public String getAlertPermission() {
        return config.getString("alerts.staff_permission", "ac.alerts");
    }

    private int[] getRange(String path, int defMin, int defMax) {
        String raw = config.getString(path, null);
        if (raw != null) {
            raw = raw.replace("[", "").replace("]", "");
            String[] parts = raw.split(",");
            if (parts.length >= 2) {
                try {
                    int a = Integer.parseInt(parts[0].trim());
                    int b = Integer.parseInt(parts[1].trim());
                    return new int[]{a, b};
                } catch (NumberFormatException ignored) {}
            }
        }
        return new int[]{defMin, defMax};
    }

    public int getAlertDelayMin() {
        return getRange("alerts.delay_seconds", 5, 10)[0];
    }

    public int getAlertDelayMax() {
        return getRange("alerts.delay_seconds", 5, 10)[1];
    }

    public int getAlertRateLimit() {
        return config.getInt("alerts.rate_limit_per_player_seconds", 3);
    }

    public int getAlertBatchWindow() {
        return config.getInt("alerts.batch_window_seconds", 2);
    }

    public int[] getTierDelaySeconds(PunishmentTier tier) {
        switch (tier) {
            case CRITICAL:
                return getRange("punishments.tiers.CRITICAL.delay_seconds", 30, 60);
            case HIGH:
                int[] high = getRange("punishments.tiers.HIGH.delay_minutes", 2, 4);
                return new int[]{high[0] * 60, high[1] * 60};
            case MEDIUM:
                int[] med = getRange("punishments.tiers.MEDIUM.delay_minutes", 4, 7);
                return new int[]{med[0] * 60, med[1] * 60};
            default:
                return new int[]{0, 0};
        }
    }

    public int getMitigationApplyDelayMin() {
        return getRange("mitigation.apply_delay_seconds", 5, 15)[0];
    }

    public int getMitigationApplyDelayMax() {
        return getRange("mitigation.apply_delay_seconds", 5, 15)[1];
    }

    public int getMitigationDurationMin() {
        return getRange("mitigation.duration_seconds", 60, 120)[0];
    }

    public int getMitigationDurationMax() {
        return getRange("mitigation.duration_seconds", 60, 120)[1];
    }

    public double getMaxDamageReduction() {
        return Double.parseDouble(config.getString("mitigation.max_damage_reduction", "0.90"));
    }

    public boolean isDetectionsDebug() {
        return config.getBoolean("logging.detections_debug", false);
    }

    public int getMinFamiliesForBan() {
        return config.getInt("policy.min_independent_families_for_ban", 2);
    }

    public boolean isMultiWindowConfirmationRequired() {
        return config.getBoolean("policy.require_multi_window_confirmation", true);
    }

    public PunishmentTier getCheckTier(String checkName) {
        String key = checkName.replace('-', '_').toUpperCase();
        String path = "checks_registry." + key;
        String value = config.getString(path, "MEDIUM").toUpperCase();
        try {
            return PunishmentTier.valueOf(value);
        } catch (IllegalArgumentException e) {
            return PunishmentTier.MEDIUM;
        }
    }
}

