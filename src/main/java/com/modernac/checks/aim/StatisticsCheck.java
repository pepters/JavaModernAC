package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.logging.DebugLogger;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class StatisticsCheck extends AimCheck {
  private final DebugLogger logger;

  public StatisticsCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Statistics", false);
    this.logger = plugin.getDebugLogger();
  }

  private int count;
  private double sumYaw, sumSqYaw;

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    logger.log(data.getUuid() + " handled Statistics");
    count++;
    sumYaw += rot.getYawChange();
    sumSqYaw += rot.getYawChange() * rot.getYawChange();
    if (count >= 25) {
      double mean = sumYaw / count;
      double variance = (sumSqYaw / count) - (mean * mean);
      if (variance < 0.02) {
        fail(1, true);
      }
      count = 0;
      sumYaw = sumSqYaw = 0;
    }
  }
}
