package com.modernac.checks.signatures.liquidbounce;

import com.modernac.ModernACPlugin;
import com.modernac.checks.aim.AimCheck;
import com.modernac.engine.DetectionResult;
import com.modernac.engine.Window;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.util.MathUtil;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Detects LiquidBounce's Tween-like smooth aimbot behaviour. Looks for very low jerk and
 * near-linear yaw progression over several ticks.
 */
public class LBTweenCheck extends AimCheck {
  private final Deque<Double> yawDeltas = new ArrayDeque<>();
  private static final int STREAK_LIMIT = 2;
  private int streak;

  public LBTweenCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "LBTween", false);
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
    synchronized (yawDeltas) {
      if (yawDeltas.size() >= 6) {
        yawDeltas.pollFirst();
      }
      yawDeltas.addLast(yaw);
    }
    double[] arr = MathUtil.snapshotNonNull(yawDeltas);
    if (arr.length < 6) {
      return;
    }
    double sum = 0.0;
    for (double v : arr) {
      sum += v;
    }
    double avg = sum / arr.length;
    double var = 0.0;
    for (double v : arr) {
      double diff = v - avg;
      var += diff * diff;
    }
    var /= arr.length;
    double std = Math.sqrt(var);
    double ratioMax = 0.0;
    for (int i = 1; i < arr.length; i++) {
      double prev = arr[i - 1];
      if (prev != 0) {
        double ratio = Math.abs(arr[i] / prev - 1);
        if (ratio > ratioMax) ratioMax = ratio;
      }
    }
    if (std < 0.01 && ratioMax < 0.02) {
      streak++;
      if (streak >= STREAK_LIMIT) {
        streak = 0;
        DetectionResult result =
            new DetectionResult(getName(), 0.9, Window.LONG, true, true, true);
        fail(result);
      }
    } else {
      streak = 0;
    }
  }
}
