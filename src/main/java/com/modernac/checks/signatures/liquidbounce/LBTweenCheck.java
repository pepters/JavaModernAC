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
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Detects LiquidBounce's Tween-like smooth aimbot behaviour. Looks for very low jerk and
 * near-linear yaw progression over several ticks.
 */
public class LBTweenCheck extends AimCheck {
  private final Deque<Double> yawDeltas = new ArrayDeque<>();

  public LBTweenCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "LB-Tween", false);
  }

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
      DetectionResult result =
          new DetectionResult("GEOMETRY", 0.9, Window.LONG, true, true, true);
      fail(result);
    }
  }
}
