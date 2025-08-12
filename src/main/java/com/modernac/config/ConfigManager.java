package com.modernac.config;

import com.modernac.ModernACPlugin;
import org.bukkit.configuration.file.FileConfiguration;

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
        return Double.parseDouble(config.getString("latency.tps_soft_guard", "16.0"));
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

    public int getPunishmentDelayMin() {
        return getRange("punishments.decision_delay_minutes", 2, 7)[0];
    }

    public int getPunishmentDelayMax() {
        return getRange("punishments.decision_delay_minutes", 2, 7)[1];
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
}

