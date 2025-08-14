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
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DistinctCheck extends AimCheck {
  private static final String FAMILY = "AIM/Patterns";

  public DistinctCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Distinct", false);
  }

  private static final int MIN_SAMPLES = 8;
  private static final double EPS = 1e-6;

  private final Deque<Double> lastYaw = new ArrayDeque<>();

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
    Player player = Bukkit.getPlayer(data.getUuid());
    double[] tpsArr = Bukkit.getTPS();
    double tps = tpsArr.length > 0 && Double.isFinite(tpsArr[0]) ? tpsArr[0] : 20.0;
    int ping = player != null ? player.getPing() : 0;
    if (ping > 180 || tps < 18.0) {
      trace("gate-fail ping=" + ping + ", tps=" + String.format(Locale.US, "%.1f", tps));
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
    int distinct = 0;
    double prev = Double.NaN;
    for (double v : arr) {
      if (distinct == 0 || Math.abs(v - prev) > EPS) {
        distinct++;
        prev = v;
      }
    }
    if (distinct <= 2) {
      DetectionResult result =
          new DetectionResult(FAMILY, 0.9, Window.SHORT, true, true, true);
      fail(result);
    }
  }
}
