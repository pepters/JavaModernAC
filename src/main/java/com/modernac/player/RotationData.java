package com.modernac.player;

public class RotationData {
  private final double yawChange;
  private final double pitchChange;
  private final long serverTime;
  private final boolean stable;

  public RotationData(double yawChange, double pitchChange, long serverTime, boolean stable) {
    this.yawChange = yawChange;
    this.pitchChange = pitchChange;
    this.serverTime = serverTime;
    this.stable = stable;
  }

  public double getYawChange() {
    return yawChange;
  }

  public double getPitchChange() {
    return pitchChange;
  }

  public long getServerTime() {
    return serverTime;
  }

  public boolean isStable() {
    return stable;
  }
}
