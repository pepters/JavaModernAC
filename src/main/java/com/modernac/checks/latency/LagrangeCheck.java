package com.modernac.checks.latency;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class LagrangeCheck extends LatencyCheck {
  private long lastPacket;
  private int streak;

  public LagrangeCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Lagrange");
  }

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    long now = System.currentTimeMillis();
    if (lastPacket != 0) {
      long delay = now - lastPacket;
      if (delay > plugin.getConfigManager().getUnstableConnectionLimit()) {
        streak++;
        if (streak > 3) {
          fail(1, true);
          streak = 0;
        }
      } else {
        streak = 0;
      }
    }
    lastPacket = now;
  }
}
