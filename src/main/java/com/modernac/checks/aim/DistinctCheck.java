package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class DistinctCheck extends AimCheck {
    private final DebugLogger logger;
    public DistinctCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Distinct", false);
        this.logger = plugin.getDebugLogger();
    }

    private final java.util.Deque<Double> lastYaw = new java.util.ArrayDeque<>();

    @Override
    public void handle(Object packet) {
        if (!(packet instanceof RotationData)) {
            return;
        }
        RotationData rot = (RotationData) packet;
        logger.log(data.getUuid() + " handled Distinct");
        lastYaw.add(rot.getYawChange());
        if (lastYaw.size() > 10) {
            lastYaw.pollFirst();
        }
        if (lastYaw.size() == 10) {
            long distinct = lastYaw.stream().distinct().count();
            if (distinct <= 2) {
                fail(1, true);
            }
        }
    }
}
