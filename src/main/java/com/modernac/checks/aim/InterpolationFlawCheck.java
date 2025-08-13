package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.logging.DebugLogger;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class InterpolationFlawCheck extends AimCheck {
  private final DebugLogger logger;

  public InterpolationFlawCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Interpolation Flaw", false);
    this.logger = plugin.getDebugLogger();
  }

  private double lastYaw;

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    logger.log(data.getUuid() + " handled Interpolation Flaw");
    if (lastYaw != 0
        && Math.signum(lastYaw) != Math.signum(rot.getYawChange())
        && Math.abs(lastYaw - rot.getYawChange()) > 30) {
      fail(1, true);
    }
    lastYaw = rot.getYawChange();
  }
}
