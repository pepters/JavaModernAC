package com.modernac.checks.signatures.liquidbounce;

import com.modernac.ModernACPlugin;
import com.modernac.checks.aim.AimCheck;
import com.modernac.engine.DetectionResult;
import com.modernac.engine.Window;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import java.util.ArrayDeque;
import java.util.Deque;

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
    if (!(packet instanceof RotationData)) return;
    RotationData rot = (RotationData) packet;
    yawDeltas.add(rot.getYawChange());
    if (yawDeltas.size() == 6) {
      double avg = yawDeltas.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
      double variance =
          yawDeltas.stream().mapToDouble(d -> (d - avg) * (d - avg)).sum() / yawDeltas.size();
      double std = Math.sqrt(variance);
      double ratioMax = 0.0;
      Double prev = null;
      for (double d : yawDeltas) {
        if (prev != null && prev != 0) {
          ratioMax = Math.max(ratioMax, Math.abs((d / prev) - 1));
        }
        prev = d;
      }
      if (std < 0.01 && ratioMax < 0.02) {
        DetectionResult result =
            new DetectionResult("GEOMETRY", 0.9, Window.LONG, true, true, true);
        fail(result);
      }
      yawDeltas.poll();
    }
  }
}
