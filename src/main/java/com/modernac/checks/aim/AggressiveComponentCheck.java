package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class AggressiveComponentCheck extends AimCheck {

  public AggressiveComponentCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Aggressive Component", true);
  }

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rotation = (RotationData) packet;
    double threshold = 5 * plugin.getConfigManager().getCombatTolerance().getMultiplier();
    trace("handled Aggressive Component");
    if (Math.abs(rotation.getPitchChange()) > threshold) {
      fail(1, true);
    }
  }
}
