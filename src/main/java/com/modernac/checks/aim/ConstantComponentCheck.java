package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class ConstantComponentCheck extends AimCheck {
    private final DebugLogger logger;
    public ConstantComponentCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Constant Component", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Constant Component detection
        logger.log(data.getUuid() + " handled Constant Component");
    }
}
