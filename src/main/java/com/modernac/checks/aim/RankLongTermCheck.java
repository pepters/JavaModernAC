package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class RankLongTermCheck extends AimCheck {
    private final DebugLogger logger;
    public RankLongTermCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Rank Long-term", false);
        this.logger = plugin.getDebugLogger();
    }

    private final java.util.Deque<Double> samples = new java.util.ArrayDeque<>();

    @Override
    public void handle(Object packet) {
        if (!(packet instanceof RotationData)) {
            return;
        }
        RotationData rot = (RotationData) packet;
        logger.log(data.getUuid() + " handled Rank Long-term");
        samples.add(rot.getYawChange());
        if (samples.size() > 1000) {
            samples.pollFirst();
        }
        if (samples.size() == 1000) {
            long distinct = samples.stream().distinct().count();
            if (distinct < 50) {
                fail(1, true);
            }
        }
    }
}
