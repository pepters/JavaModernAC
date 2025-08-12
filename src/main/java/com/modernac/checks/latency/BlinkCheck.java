package com.modernac.checks.latency;

import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.ModernACPlugin;

public class BlinkCheck extends LatencyCheck {
    private long lastPacket;

    public BlinkCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Blink");
    }

    @Override
    public void handle(Object packet) {
        if (!(packet instanceof RotationData)) {
            return;
        }
        long now = System.currentTimeMillis();
        if (lastPacket != 0) {
            long delay = now - lastPacket;
            if (delay > plugin.getConfigManager().getUnstableConnectionLimit()) {
                fail(1, false);
            }
        }
        lastPacket = now;
    }
}
