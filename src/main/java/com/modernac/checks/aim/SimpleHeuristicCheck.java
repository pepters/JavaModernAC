package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.logging.DebugLogger;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class SimpleHeuristicCheck extends AimCheck {
  private final DebugLogger logger;

  public SimpleHeuristicCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Simple heuristic", false);
    this.logger = plugin.getDebugLogger();
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
