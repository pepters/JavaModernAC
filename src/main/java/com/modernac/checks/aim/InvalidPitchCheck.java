package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class InvalidPitchCheck extends AimCheck {
    private final DebugLogger logger;
    public InvalidPitchCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Invalid pitch", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Invalid pitch detection
        logger.log(data.getUuid() + " handled Invalid pitch");
    }
}
