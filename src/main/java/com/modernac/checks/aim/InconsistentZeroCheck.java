package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class InconsistentZeroCheck extends AimCheck {

  public InconsistentZeroCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Inconsistent Zero", false);
  }

  private int streak;

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    trace("handled Inconsistent Zero");
    boolean inconsistent =
        (rot.getYawChange() == 0 && Math.abs(rot.getPitchChange()) > 1)
            || (rot.getPitchChange() == 0 && Math.abs(rot.getYawChange()) > 1);
    if (inconsistent) {
      if (++streak > 3) {
        fail(1, true);
      }
    } else {
      streak = 0;
    }
  }
}
