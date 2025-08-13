package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.util.MathUtil;
import java.util.Arrays;
import java.util.Deque;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PatternStatisticsCheck extends AimCheck {

  public PatternStatisticsCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Pattern Statistics", false);
  }

  private static final int MIN_SAMPLES = 6;
  private static final double EPS = 1e-6;

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
      fail(1, true);
    }
  }
}
