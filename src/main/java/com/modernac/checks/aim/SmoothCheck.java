package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class SmoothCheck extends AimCheck {
    private final DebugLogger logger;
    public SmoothCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Smooth", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Smooth detection
        logger.log(data.getUuid() + " handled Smooth");
    }
}
