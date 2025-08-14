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

public class RankCheck extends AimCheck {

  public RankCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Rank", false);
  }

  private static final int MIN_SAMPLES = 50;

  private final Deque<Double> samples = new ArrayDeque<>();

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
      DetectionResult result =
          new DetectionResult(getName(), 1.0, Window.SHORT, true, true, true);
      fail(result);
    } else if (pct <= 0.05 || pct >= 0.95) {
      DetectionResult result =
          new DetectionResult(getName(), 0.9, Window.SHORT, true, true, true);
      fail(result);
    }
  }
}
