package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.player.RotationPacket;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class SimpleHeuristicCheck extends AimCheck {
    private final DebugLogger logger;
    private float lastYaw;
    private float lastPitch;
    private long lastTime;
    private boolean initialized;

    public SimpleHeuristicCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Simple heuristic", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        if (!(packet instanceof RotationPacket)) {
            return;
        }

        RotationPacket rot = (RotationPacket) packet;
        logger.log(data.getUuid() + " handled Simple heuristic");

        float yaw = rot.getYaw();
        float pitch = rot.getPitch();
        long time = rot.getTimestamp();

        if (initialized) {
            float yawDiff = Math.abs(yaw - lastYaw);
            float pitchDiff = Math.abs(pitch - lastPitch);
            long deltaTime = time - lastTime;

            if (deltaTime > 0) {
                double multiplier = plugin.getConfigManager().getCombatTolerance().getMultiplier();
                double speed = (yawDiff + pitchDiff) / deltaTime;
                double speedThreshold = 2.0 * multiplier;

                if (speed > speedThreshold) {
                    fail(1, true);
                }
            }
        }

        lastYaw = yaw;
        lastPitch = pitch;
        lastTime = time;
        initialized = true;
    }
}
