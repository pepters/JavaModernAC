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

public class RankLongTermCheck extends AimCheck {

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
    Player player = Bukkit.getPlayer(data.getUuid());
    double[] tpsArr = Bukkit.getTPS();
    double tps = tpsArr.length > 0 && Double.isFinite(tpsArr[0]) ? tpsArr[0] : 20.0;
    int ping = player != null ? player.getPing() : 0;
    if (ping > 180 || tps < 18.0) {
      trace("gate-fail ping=" + ping + ", tps=" + String.format(Locale.US, "%.1f", tps));
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
      fail(1, true);
    }
  }
}
