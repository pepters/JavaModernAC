package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class PatternAnalysisCheck extends AimCheck {
    private final DebugLogger logger;
    public PatternAnalysisCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Pattern Analysis", false);
        this.logger = plugin.getDebugLogger();
    }

    private final java.util.Deque<Double> lastYaw = new java.util.ArrayDeque<>();

    @Override
    public void handle(Object packet) {
        if (!(packet instanceof RotationData)) {
            return;
        }
        RotationData rot = (RotationData) packet;
        logger.log(data.getUuid() + " handled Pattern Analysis");
        lastYaw.add(rot.getYawChange());
        if (lastYaw.size() > 4) {
            lastYaw.pollFirst();
        }
        if (lastYaw.size() == 4) {
            Double[] arr = lastYaw.toArray(new Double[0]);
            if (arr[0].equals(arr[2]) && arr[1].equals(arr[3])) {
                fail(1, true);
            }
        }
    }
}
