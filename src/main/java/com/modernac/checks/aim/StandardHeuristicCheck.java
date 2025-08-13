package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class StandardHeuristicCheck extends AimCheck {

  public StandardHeuristicCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Standard heuristic", false);
  }

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    trace("handled Standard heuristic");
    if (Math.abs(rot.getYawChange()) > 90 && Math.abs(rot.getPitchChange()) < 1) {
      fail(1, true);
    }
  }
}
