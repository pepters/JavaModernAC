package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class StandardHeuristicCheck extends AimCheck {
    private final DebugLogger logger;
    public StandardHeuristicCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Standard heuristic", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        if (!(packet instanceof RotationData)) {
            return;
        }
        RotationData rot = (RotationData) packet;
        logger.log(data.getUuid() + " handled Standard heuristic");
        if (Math.abs(rot.getYawChange()) > 90 && Math.abs(rot.getPitchChange()) < 1) {
            fail(1, true);
        }
    }
}
