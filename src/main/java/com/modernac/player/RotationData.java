package com.modernac.player;

public class RotationData {
    private final double yawChange;
    private final double pitchChange;

    public RotationData(double yawChange, double pitchChange) {
        this.yawChange = yawChange;
        this.pitchChange = pitchChange;
    }

    public double getYawChange() { return yawChange; }
    public double getPitchChange() { return pitchChange; }
}
