package com.modernac.checks.latency;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;

public class LagrangeCheck extends LatencyCheck {
    private final DebugLogger logger;
    public LagrangeCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Lagrange");
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Lagrange latency abuse detection
        logger.log(data.getUuid() + " handled Lagrange");
    }
}
