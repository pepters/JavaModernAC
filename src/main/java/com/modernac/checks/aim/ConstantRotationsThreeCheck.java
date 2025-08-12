package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class ConstantRotationsThreeCheck extends AimCheck {
    private final DebugLogger logger;
    public ConstantRotationsThreeCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Constant rotations 3", false);
        this.logger = plugin.getDebugLogger();
    }

    private double lastYaw, lastPitch;
    private int streak;

    @Override
    public void handle(Object packet) {
        if (!(packet instanceof RotationData)) {
            return;
        }
        RotationData rot = (RotationData) packet;
        logger.log(data.getUuid() + " handled Constant rotations 3");
        if (rot.getYawChange() == lastYaw && rot.getPitchChange() == lastPitch) {
            if (++streak > 6) {
                fail(1, true);
            }
        } else {
            streak = 0;
        }
        lastYaw = rot.getYawChange();
        lastPitch = rot.getPitchChange();
    }
}
