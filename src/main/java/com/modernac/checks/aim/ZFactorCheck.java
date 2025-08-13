package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.util.MathUtil;
import java.util.Deque;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ZFactorCheck extends AimCheck {

  public ZFactorCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "zFactor", false);
  }

  private static final int MIN_SAMPLES = 8;
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
    Player player = Bukkit.getPlayer(data.getUuid());
    double[] tpsArr = Bukkit.getTPS();
    double tps = tpsArr.length > 0 && Double.isFinite(tpsArr[0]) ? tpsArr[0] : 20.0;
    int ping = player != null ? player.getPing() : 0;
    if (ping > 180 || tps < 18.0) {
      trace(
          "gate-fail ping="
              + ping
              + ", tps="
              + String.format(Locale.US, "%.1f", tps));
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
    double z = Math.abs(latest - mean) / std;
    if (z >= 6.0) {
      fail(100, true);
    } else if (z >= 4.0) {
      fail(50, true);
    }
  }
}
