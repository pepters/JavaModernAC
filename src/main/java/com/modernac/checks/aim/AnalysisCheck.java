package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class AnalysisCheck extends AimCheck {

  public AnalysisCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Analysis", false);
  }

  private int count;
  private double sumYaw, sumSqYaw;

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    trace("handled Analysis");
    count++;
    sumYaw += rot.getYawChange();
    sumSqYaw += rot.getYawChange() * rot.getYawChange();
    if (count >= 100) {
      double mean = sumYaw / count;
      double variance = (sumSqYaw / count) - (mean * mean);
      if (variance < 0.05) {
        fail(1, true);
      }
      count = 0;
      sumYaw = sumSqYaw = 0;
    }
  }
}
