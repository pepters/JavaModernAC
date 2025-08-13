package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class FactorCheck extends AimCheck {

  public FactorCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Factor", false);
  }

  private double lastRatio;
  private int streak;

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    trace("handled Factor");
    if (rot.getPitchChange() != 0) {
      double ratio = rot.getYawChange() / rot.getPitchChange();
      if (Math.abs(ratio - lastRatio) < 0.01) {
        if (++streak > 8) {
          fail(1, true);
        }
      } else {
        streak = 0;
      }
      lastRatio = ratio;
    }
  }
}
