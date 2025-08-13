package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import java.util.Arrays;
import java.util.Deque;

public class ZFactorCheck extends AimCheck {

  public ZFactorCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "zFactor", false);
  }

  private static final int MIN_SAMPLES = 8;
  private static final double EPS = 1e-6;

  private final Deque<Double> samples = new java.util.ArrayDeque<>();

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
    synchronized (samples) {
      if (samples.size() >= 20) {
        samples.pollFirst();
      }
      samples.addLast(yaw);
    }
    double[] arr = snapshotNonNull(samples);
    if (arr.length < MIN_SAMPLES) {
      return;
    }
    double latest = arr[arr.length - 1];
    int n = arr.length - 1;
    double sum = 0, sum2 = 0;
    for (int i = 0; i < n; i++) {
      double v = arr[i];
      sum += v;
      sum2 += v * v;
    }
    double mean = sum / n;
    double var = (sum2 / n) - mean * mean;
    if (var <= EPS) {
      return;
    }
    double std = Math.sqrt(var);
    double z = Math.abs(latest - mean) / std;
    if (z >= 6.0) {
      fail(100, true);
    } else if (z >= 4.0) {
      fail(50, true);
    }
  }
}
