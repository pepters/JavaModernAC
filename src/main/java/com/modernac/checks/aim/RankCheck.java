package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class RankCheck extends AimCheck {
    private final DebugLogger logger;
    public RankCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Rank", false);
        this.logger = plugin.getDebugLogger();
    }

    private final java.util.Deque<Double> samples = new java.util.ArrayDeque<>();

    @Override
    public void handle(Object packet) {
        if (!(packet instanceof RotationData)) {
            return;
        }
        RotationData rot = (RotationData) packet;
        logger.log(data.getUuid() + " handled Rank");
        samples.add(rot.getYawChange());
        if (samples.size() > 50) {
            samples.pollFirst();
        }
        if (samples.size() == 50) {
            java.util.List<Double> sorted = samples.stream().sorted().collect(java.util.stream.Collectors.toList());
            int index = sorted.indexOf(rot.getYawChange());
            if (index == 0 || index == sorted.size() - 1) {
                fail(1, true);
            }
        }
    }
}
