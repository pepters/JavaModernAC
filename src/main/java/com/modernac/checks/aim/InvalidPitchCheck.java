package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.logging.DebugLogger;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class InvalidPitchCheck extends AimCheck {
  private final DebugLogger logger;

  public InvalidPitchCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Invalid pitch", false);
    this.logger = plugin.getDebugLogger();
  }

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    logger.log(data.getUuid() + " handled Invalid pitch");
    double pitch = rot.getPitchChange();
    if (pitch > 90 || pitch < -90) {
      fail(1, true);
    }
  }
}
