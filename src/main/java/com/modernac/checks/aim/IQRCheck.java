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

public class IQRCheck extends AimCheck {

  public IQRCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "IQR", false);
  }

  private static final int MIN_SAMPLES = 8;
  private final Deque<Double> window = new ArrayDeque<>();

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
    synchronized (window) {
      if (window.size() >= 40) {
        window.pollFirst();
      }
      window.addLast(yaw);
    }
    double[] arr = MathUtil.snapshotNonNull(window);
    if (arr.length < MIN_SAMPLES) {
      return;
    }
    Arrays.sort(arr);
    double median = quartile(arr, 0.5);
    double dist = Math.abs(yaw - median);
    double q1 = quartile(arr, 0.25);
    double q3 = quartile(arr, 0.75);
    double iqr = q3 - q1;
    if (iqr <= 0) {
      return;
    }
    if (dist > 3 * iqr) {
      DetectionResult result = new DetectionResult(getName(), 1.0, Window.SHORT, true, true, true);
      fail(result);
    } else if (dist > 1.5 * iqr) {
      DetectionResult result = new DetectionResult(getName(), 0.9, Window.SHORT, true, true, true);
      fail(result);
    }
  }

  private static double quartile(double[] a, double q) {
    int n = a.length;
    double pos = q * (n - 1);
    int idx = (int) pos;
    double frac = pos - idx;
    double base = a[idx];
    return idx + 1 < n ? base + (a[idx + 1] - base) * frac : base;
  }
}
