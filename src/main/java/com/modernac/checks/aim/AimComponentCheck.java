package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class AimComponentCheck extends AimCheck {
    private final DebugLogger logger;
    public AimComponentCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Aim Component", false);
        this.logger = plugin.getDebugLogger();
    }

    private double lastRatio;
    private int streak;

    @Override
    public void handle(Object packet) {
        if (!(packet instanceof RotationData)) {
            return;
        }
        RotationData rot = (RotationData) packet;
        logger.log(data.getUuid() + " handled Aim Component");
        if (rot.getPitchChange() != 0) {
            double ratio = rot.getYawChange() / rot.getPitchChange();
            if (Math.abs(ratio - lastRatio) < 0.005) {
                if (++streak > 20) {
                    fail(1, true);
                }
            } else {
                streak = 0;
            }
            lastRatio = ratio;
        }
    }
}
