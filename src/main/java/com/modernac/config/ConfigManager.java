package com.modernac.config;

import org.bukkit.configuration.file.FileConfiguration;
import com.modernac.ModernACPlugin;

public class ConfigManager {
    private final ModernACPlugin plugin;
    private final FileConfiguration config;

    public ConfigManager(ModernACPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public String getKickCommand() {
        return config.getString("commands.kick", "kick %player%");
    }

    public String getBanCommand() {
        return config.getString("commands.ban", "ban %player%");
    }

    public int getUnstableConnectionLimit() {
        return config.getInt("unstable_connection_limit", 1750);
    }

    public boolean isExperimentalDetections() {
        return config.getBoolean("experimental_detections", false);
    }

    public CombatTolerance getCombatTolerance() {
        String value = config.getString("combat_tolerance", "NORMAL").toUpperCase();
        try {
            return CombatTolerance.valueOf(value);
        } catch (IllegalArgumentException e) {
            return CombatTolerance.NORMAL;
        }
    }

    public int getAlertDelayMin() {
        return config.getInt("alert_delay_min", 5);
    }

    public int getAlertDelayMax() {
        return config.getInt("alert_delay_max", 10);
    }

    public int getPunishmentDelayMin() {
        return config.getInt("punishment_delay_min", 2);
    }

    public int getPunishmentDelayMax() {
        return config.getInt("punishment_delay_max", 7);
    }

    public int getMitigationDurationSeconds() {
        return config.getInt("mitigation_duration_seconds", 60);
    }

    public int getMaxMitigationReduction() {
        return config.getInt("max_mitigation_reduction", 90);
    }
}
