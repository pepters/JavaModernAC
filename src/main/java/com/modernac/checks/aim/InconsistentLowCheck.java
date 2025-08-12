package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class InconsistentLowCheck extends AimCheck {
    private final DebugLogger logger;
    public InconsistentLowCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Inconsistent Too low", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Inconsistent Too low detection
        logger.log(data.getUuid() + " handled Inconsistent Too low");
    }
}
