package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class AnalysisCheck extends AimCheck {
    private final DebugLogger logger;
    public AnalysisCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Analysis", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Analysis detection
        logger.log(data.getUuid() + " handled Analysis");
    }
}
