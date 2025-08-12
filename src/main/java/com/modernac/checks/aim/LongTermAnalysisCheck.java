package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class LongTermAnalysisCheck extends AimCheck {
    private final DebugLogger logger;
    public LongTermAnalysisCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Long-term Analysis", false);
        this.logger = plugin.getDebugLogger();
    }

    private int count;
    private double sumYaw, sumSqYaw;

    @Override
    public void handle(Object packet) {
        if (!(packet instanceof RotationData)) {
            return;
        }
        RotationData rot = (RotationData) packet;
        logger.log(data.getUuid() + " handled Long-term Analysis");
        count++;
        sumYaw += rot.getYawChange();
        sumSqYaw += rot.getYawChange() * rot.getYawChange();
        if (count >= 1000) {
            double mean = sumYaw / count;
            double variance = (sumSqYaw / count) - (mean * mean);
            if (variance < 0.01) {
                fail(1, true);
            }
            count = 0;
            sumYaw = sumSqYaw = 0;
        }
    }
}
