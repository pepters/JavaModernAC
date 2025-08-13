package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class ZFactorCheck extends AimCheck {

  public ZFactorCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "zFactor", false);
  }

  private final java.util.Deque<Double> samples = new java.util.ArrayDeque<>();

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    trace("handled zFactor");
    samples.add(rot.getYawChange());
    if (samples.size() > 20) {
      samples.pollFirst();
    }
    if (samples.size() == 20) {
      double mean = samples.stream().mapToDouble(d -> d).average().orElse(0.0);
      double variance =
          samples.stream().mapToDouble(d -> (d - mean) * (d - mean)).sum() / samples.size();
      double std = Math.sqrt(variance);
      double z = std == 0 ? 0 : Math.abs(rot.getYawChange() - mean) / std;
      if (z > 3) {
        fail(1, true);
      }
    }
  }
}
