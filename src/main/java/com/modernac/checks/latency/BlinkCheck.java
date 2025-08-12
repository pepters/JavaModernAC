package com.modernac.checks.latency;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class BlinkCheck extends LatencyCheck {
    private final DebugLogger logger;
    public BlinkCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Blink");
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Blink detection
        logger.log(data.getUuid() + " handled Blink");
    }
}
