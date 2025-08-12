package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class ConstantRotationsOneCheck extends AimCheck {
    private final DebugLogger logger;
    public ConstantRotationsOneCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Constant rotations 1", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Constant rotations 1 detection
        logger.log(data.getUuid() + " handled Constant rotations 1");
    }
}
