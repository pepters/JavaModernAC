package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class StatisticsCheck extends AimCheck {
    private final DebugLogger logger;
    public StatisticsCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Statistics", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Statistics detection
        logger.log(data.getUuid() + " handled Statistics");
    }
}
