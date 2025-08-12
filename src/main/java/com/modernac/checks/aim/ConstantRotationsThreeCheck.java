package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class ConstantRotationsThreeCheck extends AimCheck {
    private final DebugLogger logger;
    public ConstantRotationsThreeCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Constant rotations 3", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Constant rotations 3 detection
        logger.log(data.getUuid() + " handled Constant rotations 3");
    }
}
