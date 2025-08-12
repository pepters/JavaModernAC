package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class DistinctCheck extends AimCheck {
    private final DebugLogger logger;
    public DistinctCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Distinct", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Distinct detection
        logger.log(data.getUuid() + " handled Distinct");
    }
}
