package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.logging.DebugLogger;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class SyncComponentCheck extends AimCheck {
  private final DebugLogger logger;

  public SyncComponentCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Sync Component", false);
    this.logger = plugin.getDebugLogger();
  }

  private int streak;

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    logger.log(data.getUuid() + " handled Sync Component");
    if (Math.abs(Math.abs(rot.getYawChange()) - Math.abs(rot.getPitchChange())) < 0.1) {
      if (++streak > 15) {
        fail(1, true);
      }
    } else {
      streak = 0;
    }
  }
}
