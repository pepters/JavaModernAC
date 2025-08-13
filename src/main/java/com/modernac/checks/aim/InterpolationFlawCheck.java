package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class InterpolationFlawCheck extends AimCheck {

  public InterpolationFlawCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Interpolation Flaw", false);
  }

  private double lastYaw;

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    trace("handled Interpolation Flaw");
    if (lastYaw != 0
        && Math.signum(lastYaw) != Math.signum(rot.getYawChange())
        && Math.abs(lastYaw - rot.getYawChange()) > 30) {
      fail(1, true);
    }
    lastYaw = rot.getYawChange();
  }
}
