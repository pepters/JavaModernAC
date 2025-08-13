package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class ConstantRotationsOneCheck extends AimCheck {

  public ConstantRotationsOneCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Constant rotations 1", false);
  }

  private double lastYaw;
  private int streak;

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    trace("handled Constant rotations 1");
    if (rot.getYawChange() == lastYaw) {
      if (++streak > 8) {
        fail(1, true);
      }
    } else {
      streak = 0;
    }
    lastYaw = rot.getYawChange();
  }
}
