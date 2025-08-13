package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class ConstantComponentCheck extends AimCheck {

  public ConstantComponentCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Constant Component", false);
  }

  private double lastYaw, lastPitch;
  private int streak;

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    trace("handled Constant Component");
    if (rot.getYawChange() == lastYaw && rot.getPitchChange() == lastPitch) {
      if (++streak > 10) {
        fail(1, true);
      }
    } else {
      streak = 0;
    }
    lastYaw = rot.getYawChange();
    lastPitch = rot.getPitchChange();
  }
}
