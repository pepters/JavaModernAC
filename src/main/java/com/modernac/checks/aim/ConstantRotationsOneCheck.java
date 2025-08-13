package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.logging.DebugLogger;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class ConstantRotationsOneCheck extends AimCheck {
  private final DebugLogger logger;

  public ConstantRotationsOneCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Constant rotations 1", false);
    this.logger = plugin.getDebugLogger();
  }

  private double lastYaw;
  private int streak;

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    logger.log(data.getUuid() + " handled Constant rotations 1");
    if (rot.getYawChange() == lastYaw) {
      if (++streak > 8) {
        fail(1, true);
      }
    } else {
      streak = 0;
    }
    lastYaw = rot.getYawChange();
  }
}
