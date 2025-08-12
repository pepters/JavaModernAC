package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class PatternStatisticsCheck extends AimCheck {
    private final DebugLogger logger;
    public PatternStatisticsCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Pattern Statistics", false);
        this.logger = plugin.getDebugLogger();
    }

    private final java.util.Deque<Double> lastYaw = new java.util.ArrayDeque<>();

    @Override
    public void handle(Object packet) {
        if (!(packet instanceof RotationData)) {
            return;
        }
        RotationData rot = (RotationData) packet;
        logger.log(data.getUuid() + " handled Pattern Statistics");
        lastYaw.add(rot.getYawChange());
        if (lastYaw.size() > 6) {
            lastYaw.pollFirst();
        }
        if (lastYaw.size() == 6) {
            Double[] arr = lastYaw.toArray(new Double[0]);
            boolean pattern = arr[0].equals(arr[3]) && arr[1].equals(arr[4]) && arr[2].equals(arr[5]);
            if (pattern) {
                fail(1, true);
            }
        }
    }
}
