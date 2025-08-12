package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class FactorCheck extends AimCheck {
    private final DebugLogger logger;
    public FactorCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Factor", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Factor detection
        logger.log(data.getUuid() + " handled Factor");
    }
}
