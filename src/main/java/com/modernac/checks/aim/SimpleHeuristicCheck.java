package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class SimpleHeuristicCheck extends AimCheck {

  public SimpleHeuristicCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Simple heuristic", false);
  }

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rotation = (RotationData) packet;
    double threshold = 10 * plugin.getConfigManager().getCombatTolerance().getMultiplier();
    if (Math.abs(rotation.getYawChange()) > threshold) {
      fail(1, true);
    }
  }
}
