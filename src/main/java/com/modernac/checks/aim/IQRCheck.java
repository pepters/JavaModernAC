package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class IQRCheck extends AimCheck {

  public IQRCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "IQR", false);
  }

  private final java.util.Deque<Double> lastYaw = new java.util.ArrayDeque<>();

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    trace("handled IQR");
    lastYaw.add(rot.getYawChange());
    if (lastYaw.size() > 20) {
      lastYaw.pollFirst();
    }
    if (lastYaw.size() == 20) {
      double min = lastYaw.stream().min(Double::compare).orElse(0.0);
      double max = lastYaw.stream().max(Double::compare).orElse(0.0);
      if (max - min < 0.1) {
        fail(1, true);
      }
    }
  }
}
