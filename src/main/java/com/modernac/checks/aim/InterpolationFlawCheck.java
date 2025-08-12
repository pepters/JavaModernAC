package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class InterpolationFlawCheck extends AimCheck {
    private final DebugLogger logger;
    public InterpolationFlawCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Interpolation Flaw", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Interpolation Flaw detection
        logger.log(data.getUuid() + " handled Interpolation Flaw");
    }
}
