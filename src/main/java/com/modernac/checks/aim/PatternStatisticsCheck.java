package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class PatternStatisticsCheck extends AimCheck {
    private final DebugLogger logger;
    public PatternStatisticsCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Pattern Statistics", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Pattern Statistics detection
        logger.log(data.getUuid() + " handled Pattern Statistics");
    }
}
