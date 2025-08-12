package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class ZFactorCheck extends AimCheck {
    private final DebugLogger logger;
    public ZFactorCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "zFactor", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement zFactor detection
        logger.log(data.getUuid() + " handled zFactor");
    }
}
