package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.engine.DetectionResult;
import com.modernac.engine.Window;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.util.MathUtil;
import java.util.Deque;

public class ZFactorCheck extends AimCheck {
  private static final String FAMILY = "AIM/Outliers";

  public ZFactorCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "zFactor", false);
  }

  private static final int MIN_SAMPLES = 20;
  private static final double EPS = 1e-6;
  private static final int STREAK_LIMIT = 2;
  private int streak;

  private final Deque<Double> samples = new java.util.ArrayDeque<>();

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
    double[] arr = MathUtil.snapshotNonNull(samples);
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
    if (std < 0.03) {
      return;
    }
    double z = Math.abs(latest - mean) / std;
    if (z >= 6.0) {
      streak++;
      if (streak >= STREAK_LIMIT) {
        streak = 0;
        DetectionResult result =
            new DetectionResult(FAMILY, 1.0, Window.SHORT, true, true, true);
        fail(result);
      }
    } else if (z >= 4.0) {
      streak++;
      if (streak >= STREAK_LIMIT) {
        streak = 0;
        DetectionResult result =
            new DetectionResult(FAMILY, 0.9, Window.SHORT, true, true, true);
        fail(result);
      }
    } else {
      streak = 0;
    }
  }
}
