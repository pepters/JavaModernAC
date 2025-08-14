package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.engine.DetectionResult;
import com.modernac.engine.Window;
import com.modernac.net.LagCompensator;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.util.MathUtil;

import java.util.Arrays;
import java.util.Deque;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RankLongTermCheck extends AimCheck {
  private static final String FAMILY = "AIM/Outliers-SHORT";

  public RankLongTermCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Rank Long-term", false);
  }

  private static final int MIN_SAMPLES = 1000;
  private static final double EPS = 1e-6;

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
    LagCompensator.LagContext ctx = plugin.getLagCompensator().estimate(data.getUuid());
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
    Arrays.sort(arr);
    int distinct = 0;
    double prev = Double.NaN;
    for (double v : arr) {
      if (distinct == 0 || Math.abs(v - prev) > EPS) {
        distinct++;
        prev = v;
      }
    }
    if (distinct < 50) {
      double e = 0.9 * clamp(1 - ctx.jitterMs / 200.0, 0.6, 1.0);
      DetectionResult result =
          new DetectionResult(FAMILY, e, Window.LONG, true, true, true);
      fail(result);
    }
  }

  private static double clamp(double v, double lo, double hi) {
    return Math.max(lo, Math.min(hi, v));
  }
}
