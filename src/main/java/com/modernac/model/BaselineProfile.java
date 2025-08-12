package com.modernac.model;

/**
 * Per-player baseline profile capturing typical rotation behaviour.
 */
public class BaselineProfile {
    private final RollingStats yaw = new RollingStats();
    private final RollingStats pitch = new RollingStats();
    private final RollingStats jerk = new RollingStats();
    private long startTime = System.currentTimeMillis();
    private double lastYaw;
    private boolean hasLastYaw = false;

    /**
     * Update baseline with a new rotation delta.
     */
    public void update(double dyaw, double dpitch) {
        if (!isWithinBuildWindow()) return;
        yaw.add(dyaw);
        pitch.add(dpitch);
        if (hasLastYaw) {
            jerk.add(Math.abs(dyaw - lastYaw));
        }
        lastYaw = dyaw;
        hasLastYaw = true;
    }

    private boolean isWithinBuildWindow() {
        return (System.currentTimeMillis() - startTime) < 180_000; // 3 minutes
    }

    public boolean isReady() {
        return !isWithinBuildWindow();
    }

    public double zYaw(double dyaw) {
        return yaw.zScore(dyaw);
    }

    public double zPitch(double dpitch) {
        return pitch.zScore(dpitch);
    }

    public double zJerk(double djerk) {
        return jerk.zScore(djerk);
    }
}
