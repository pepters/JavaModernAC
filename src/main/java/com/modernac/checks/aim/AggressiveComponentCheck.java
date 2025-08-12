package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.player.RotationPacket;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class AggressiveComponentCheck extends AimCheck {
    private final DebugLogger logger;
    private float lastYaw;
    private float lastPitch;
    private long lastTime;
    private boolean initialized;

    public AggressiveComponentCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Aggressive Component", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        if (!(packet instanceof RotationPacket)) {
            return;
        }

        RotationPacket rot = (RotationPacket) packet;
        logger.log(data.getUuid() + " handled Aggressive Component");

        float yaw = rot.getYaw();
        float pitch = rot.getPitch();
        long time = rot.getTimestamp();

        if (initialized) {
            float yawDiff = Math.abs(yaw - lastYaw);
            float pitchDiff = Math.abs(pitch - lastPitch);
            long deltaTime = time - lastTime;

            double multiplier = plugin.getConfigManager().getCombatTolerance().getMultiplier();
            long minInterval = (long) (50 / multiplier);
            double yawThreshold = 30 * multiplier;
            double pitchThreshold = 30 * multiplier;

            if (deltaTime < minInterval && (yawDiff > yawThreshold || pitchDiff > pitchThreshold)) {
                fail(1, true);
            }
        }

        lastYaw = yaw;
        lastPitch = pitch;
        lastTime = time;
        initialized = true;
    }
}
