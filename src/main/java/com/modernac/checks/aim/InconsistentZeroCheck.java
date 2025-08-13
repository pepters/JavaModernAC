package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.logging.DebugLogger;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class InconsistentZeroCheck extends AimCheck {
  private final DebugLogger logger;

  public InconsistentZeroCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Inconsistent Zero", false);
    this.logger = plugin.getDebugLogger();
  }

  private int streak;

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    logger.log(data.getUuid() + " handled Inconsistent Zero");
    boolean inconsistent =
        (rot.getYawChange() == 0 && Math.abs(rot.getPitchChange()) > 1)
            || (rot.getPitchChange() == 0 && Math.abs(rot.getYawChange()) > 1);
    if (inconsistent) {
      if (++streak > 3) {
        fail(1, true);
      }
    } else {
      streak = 0;
    }
  }
}
