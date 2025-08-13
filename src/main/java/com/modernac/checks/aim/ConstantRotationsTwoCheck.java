package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class ConstantRotationsTwoCheck extends AimCheck {

  public ConstantRotationsTwoCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Constant rotations 2", false);
  }

  private double lastPitch;
  private int streak;

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    trace("handled Constant rotations 2");
    if (rot.getPitchChange() == lastPitch) {
      if (++streak > 8) {
        fail(1, true);
      }
    } else {
      streak = 0;
    }
    lastPitch = rot.getPitchChange();
  }
}
