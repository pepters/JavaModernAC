package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Arrays;

public class RankCheck extends AimCheck {

  public RankCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Rank", false);
  }

  private static final int MIN_SAMPLES = 50;

  private final Deque<Double> samples = new ArrayDeque<>();

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
    if (data == null) {
      return;
    }
    RotationData rot = (RotationData) packet;
    double yaw = rot.getYawChange();
    if (!Double.isFinite(yaw)) {
      return;
    }
    synchronized (samples) {
      if (samples.size() >= MIN_SAMPLES) {
        samples.pollFirst();
      }
      samples.addLast(yaw);
    }
    double[] arr = snapshotNonNull(samples);
    if (arr.length < MIN_SAMPLES) {
      return;
    }
    Arrays.sort(arr);
    int index = Arrays.binarySearch(arr, yaw);
    if (index <= 0 || index >= arr.length - 1) {
      fail(1, true);
    }
  }
}
