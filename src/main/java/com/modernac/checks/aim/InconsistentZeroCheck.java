package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class InconsistentZeroCheck extends AimCheck {
    private final DebugLogger logger;
    public InconsistentZeroCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Inconsistent Zero", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Inconsistent Zero detection
        logger.log(data.getUuid() + " handled Inconsistent Zero");
    }
}
