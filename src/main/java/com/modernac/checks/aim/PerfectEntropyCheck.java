package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.engine.DetectionResult;
import com.modernac.engine.Window;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.util.MathUtil;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PerfectEntropyCheck extends AimCheck {

  public PerfectEntropyCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "PerfectEntropy", false);
  }

  private static final int MIN_SAMPLES = 16;
  private final Deque<Double> yawSamples = new ArrayDeque<>();

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
    synchronized (yawSamples) {
      if (yawSamples.size() >= 64) {
        yawSamples.pollFirst();
      }
      yawSamples.addLast(yaw);
    }
    double[] arr = MathUtil.snapshotNonNull(yawSamples);
    if (arr.length < MIN_SAMPLES) {
      return;
    }
    double min = arr[0];
    double max = arr[0];
    for (double v : arr) {
      if (v < min) {
        min = v;
      }
      if (v > max) {
        max = v;
      }
    }
    double range = max - min;
    if (range <= 0) {
      return;
    }
    int bins = 64;
    double size = range / bins;
    int[] counts = new int[bins];
    for (double v : arr) {
      int idx = (int) ((v - min) / size);
      if (idx < 0) {
        idx = 0;
      } else if (idx >= bins) {
        idx = bins - 1;
      }
      counts[idx]++;
    }
    double h = 0.0;
    for (int c : counts) {
      if (c > 0) {
        double p = (double) c / arr.length;
        h -= p * (Math.log(p) / Math.log(2));
      }
    }
    if (h < 1.0) {
      DetectionResult result = new DetectionResult(getName(), 1.0, Window.SHORT, true, true, true);
      fail(result);
    } else if (h < 2.0) {
      DetectionResult result = new DetectionResult(getName(), 0.9, Window.SHORT, true, true, true);
      fail(result);
    }
  }
}
