package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.logging.DebugLogger;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class PerfectEntropyCheck extends AimCheck {
  private final DebugLogger logger;

  public PerfectEntropyCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Perfect/Similar shannon entropy", false);
    this.logger = plugin.getDebugLogger();
  }

  private final java.util.Deque<Double> yawSamples = new java.util.ArrayDeque<>();

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    logger.log(data.getUuid() + " handled Perfect/Similar shannon entropy");
    yawSamples.add(rot.getYawChange());
    if (yawSamples.size() > 30) {
      yawSamples.pollFirst();
    }
    if (yawSamples.size() == 30) {
      long distinct = yawSamples.stream().distinct().count();
      if (distinct <= 3) {
        fail(1, true);
      }
    }
  }
}
