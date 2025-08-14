package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.engine.DetectionResult;
import com.modernac.engine.Window;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.util.MathUtil;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class RankCheck extends AimCheck {
  private static final String FAMILY = "AIM/Outliers-SHORT";

  public RankCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Rank", false);
  }

  private static final int MIN_SAMPLES = 64;
  private static final int STREAK_LIMIT = 2;
  private int streak;

  private final Deque<Double> samples = new ArrayDeque<>();

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
      if (samples.size() >= MIN_SAMPLES) {
        samples.pollFirst();
      }
      samples.addLast(yaw);
    }
    double[] arr = MathUtil.snapshotNonNull(samples);
    if (arr.length < MIN_SAMPLES) {
      return;
    }
    double latest = arr[arr.length - 1];
    double[] prev = Arrays.copyOf(arr, arr.length - 1);
    Arrays.sort(prev);
    int pos = Arrays.binarySearch(prev, latest);
    if (pos < 0) {
      pos = -pos - 1;
    }
    double pct = prev.length > 0 ? pos / (double) prev.length : 0.5;
    if (pct <= 0.01 || pct >= 0.99) {
      streak++;
      if (streak >= STREAK_LIMIT) {
        streak = 0;
        DetectionResult result =
            new DetectionResult(FAMILY, 1.0, Window.SHORT, true, true, true);
        fail(result);
      }
    } else if (pct <= 0.05 || pct >= 0.95) {
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
