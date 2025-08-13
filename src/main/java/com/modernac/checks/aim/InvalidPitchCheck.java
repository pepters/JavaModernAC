package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class InvalidPitchCheck extends AimCheck {

  public InvalidPitchCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Invalid pitch", false);
  }

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    trace("handled Invalid pitch");
    double pitch = rot.getPitchChange();
    if (pitch > 90 || pitch < -90) {
      fail(1, true);
    }
  }
}
