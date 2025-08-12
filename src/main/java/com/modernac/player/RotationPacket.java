package com.modernac.player;

/**
 * Simple data holder representing a player's rotation at a moment in time.
 */
public class RotationPacket {
    private final float yaw;
    private final float pitch;
    private final long timestamp;

    public RotationPacket(float yaw, float pitch, long timestamp) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.timestamp = timestamp;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
