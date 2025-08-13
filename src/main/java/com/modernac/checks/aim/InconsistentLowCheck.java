package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.logging.DebugLogger;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class InconsistentLowCheck extends AimCheck {
  private final DebugLogger logger;

  public InconsistentLowCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Inconsistent Too low", false);
    this.logger = plugin.getDebugLogger();
  }

  private int streak;

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    logger.log(data.getUuid() + " handled Inconsistent Too low");
    if (Math.abs(rot.getYawChange()) < 0.05 && Math.abs(rot.getPitchChange()) > 2) {
      if (++streak > 5) {
        fail(1, true);
      }
    } else {
      streak = 0;
    }
  }
}
