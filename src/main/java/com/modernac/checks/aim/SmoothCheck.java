package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class SmoothCheck extends AimCheck {
    private final DebugLogger logger;
    public SmoothCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Smooth", false);
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
        logger.log(data.getUuid() + " handled Smooth");
        if (Math.abs(rot.getYawChange() - lastYaw) < 0.01 && Math.abs(rot.getPitchChange() - lastPitch) < 0.01) {
            if (++streak > 8) {
                fail(1, true);
            }
        } else {
            streak = 0;
        }
        lastYaw = rot.getYawChange();
        lastPitch = rot.getPitchChange();
    }
}
