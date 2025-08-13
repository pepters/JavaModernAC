package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.logging.DebugLogger;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class RandomizerFlawHeuristicCheck extends AimCheck {
  private final DebugLogger logger;

  public RandomizerFlawHeuristicCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Randomizer flaw Heuristic", false);
    this.logger = plugin.getDebugLogger();
  }

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    logger.log(data.getUuid() + " handled Randomizer flaw Heuristic");
    double pitch = Math.abs(rot.getPitchChange());
    double mod = pitch % 0.015625;
    if (mod < 1e-6) {
      fail(1, true);
    }
  }
}
