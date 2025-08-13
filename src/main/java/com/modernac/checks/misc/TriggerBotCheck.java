package com.modernac.checks.misc;

import com.modernac.ModernACPlugin;
import com.modernac.checks.Check;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class TriggerBotCheck extends Check {
  public TriggerBotCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "TriggerBot", true);
  }

  private long lastRotation;

  @Override
  public void handle(Object packet) {
    if (packet instanceof RotationData) {
      lastRotation = System.currentTimeMillis();
    } else if (packet instanceof String && packet.equals("ATTACK")) {
      if (System.currentTimeMillis() - lastRotation < 5L) {
        fail(1, false);
      }
    }
  }
}
