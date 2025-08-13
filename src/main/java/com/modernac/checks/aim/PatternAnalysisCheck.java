package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class PatternAnalysisCheck extends AimCheck {

  public PatternAnalysisCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Pattern Analysis", false);
  }

  private final java.util.Deque<Double> lastYaw = new java.util.ArrayDeque<>();

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    trace("handled Pattern Analysis");
    lastYaw.add(rot.getYawChange());
    if (lastYaw.size() > 4) {
      lastYaw.pollFirst();
    }
    if (lastYaw.size() == 4) {
      Double[] arr = lastYaw.toArray(new Double[0]);
      if (arr[0].equals(arr[2]) && arr[1].equals(arr[3])) {
        fail(1, true);
      }
    }
  }
}
