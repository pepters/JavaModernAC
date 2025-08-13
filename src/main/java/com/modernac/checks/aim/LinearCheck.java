package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.logging.DebugLogger;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class LinearCheck extends AimCheck {
  private final DebugLogger logger;

  public LinearCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Linear", false);
    this.logger = plugin.getDebugLogger();
  }

  private double lastYaw, lastDiff;
  private int streak;

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    logger.log(data.getUuid() + " handled Linear");
    double diff = rot.getYawChange() - lastYaw;
    if (Math.abs(diff - lastDiff) < 0.01) {
      if (++streak > 6) {
        fail(1, true);
      }
    } else {
      streak = 0;
    }
    lastYaw = rot.getYawChange();
    lastDiff = diff;
  }
}
