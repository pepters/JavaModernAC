package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class PatternAnalysisCheck extends AimCheck {
    private final DebugLogger logger;
    public PatternAnalysisCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Pattern Analysis", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Pattern Analysis detection
        logger.log(data.getUuid() + " handled Pattern Analysis");
    }
}
