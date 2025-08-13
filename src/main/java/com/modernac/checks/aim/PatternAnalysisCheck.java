package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import java.util.Arrays;
import java.util.Deque;

public class PatternAnalysisCheck extends AimCheck {

  public PatternAnalysisCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Pattern Analysis", false);
  }

  private static final int MIN_SAMPLES = 4;
  private static final double EPS = 1e-6;

  private final Deque<Double> lastYaw = new java.util.ArrayDeque<>();

  private static double[] snapshotNonNull(Deque<Double> q) {
    Double[] tmp;
    synchronized (q) {
      tmp = q.toArray(new Double[0]);
    }
    double[] out = new double[tmp.length];
    int n = 0;
    for (Double d : tmp) {
      if (d != null) {
        double v = d.doubleValue();
        if (!Double.isNaN(v) && !Double.isInfinite(v)) out[n++] = v;
      }
    }
    return n == out.length ? out : Arrays.copyOf(out, n);
  }

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    double yaw = rot.getYawChange();
    if (!Double.isFinite(yaw)) {
      return;
    }
    synchronized (lastYaw) {
      if (lastYaw.size() >= MIN_SAMPLES) {
        lastYaw.pollFirst();
      }
      lastYaw.addLast(yaw);
    }
    double[] arr = snapshotNonNull(lastYaw);
    if (arr.length < MIN_SAMPLES) {
      return;
    }
    Arrays.sort(arr);
    if (Math.abs(arr[0] - arr[1]) <= EPS
        && Math.abs(arr[2] - arr[3]) <= EPS
        && Math.abs(arr[1] - arr[2]) > EPS) {
      fail(1, true);
    }
  }
}
