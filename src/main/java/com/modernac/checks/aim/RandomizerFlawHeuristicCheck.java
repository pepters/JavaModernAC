package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class RandomizerFlawHeuristicCheck extends AimCheck {

  public RandomizerFlawHeuristicCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Randomizer flaw Heuristic", true);
  }

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    trace("handled Randomizer flaw Heuristic");
    double pitch = Math.abs(rot.getPitchChange());
    double mod = pitch % 0.015625;
    if (mod < 1e-6) {
      fail(1, true);
    }
  }
}
