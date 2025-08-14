package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.engine.DetectionResult;
import com.modernac.engine.Window;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.util.MathUtil;
import java.util.Arrays;
import java.util.Deque;

public class PatternStatisticsCheck extends AimCheck {
  private static final String FAMILY = "AIM/Patterns-SHORT";

  public PatternStatisticsCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Pattern Statistics", false);
  }

  private static final int MIN_SAMPLES = 8;
  private static final double EPS = 1e-6;
  private static final int STREAK_LIMIT = 2;
  private int streak;

  private final Deque<Double> lastYaw = new java.util.ArrayDeque<>();

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
    double[] arr = MathUtil.snapshotNonNull(lastYaw);
    if (arr.length < MIN_SAMPLES) {
      return;
    }
    Arrays.sort(arr);
    if (Math.abs(arr[0] - arr[1]) <= EPS
        && Math.abs(arr[2] - arr[3]) <= EPS
        && Math.abs(arr[4] - arr[5]) <= EPS
        && Math.abs(arr[1] - arr[2]) > EPS
        && Math.abs(arr[3] - arr[4]) > EPS) {
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
