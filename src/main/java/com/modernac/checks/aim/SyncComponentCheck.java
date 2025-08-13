package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class SyncComponentCheck extends AimCheck {

  public SyncComponentCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Sync Component", false);
  }

  private int streak;

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    trace("handled Sync Component");
    if (Math.abs(Math.abs(rot.getYawChange()) - Math.abs(rot.getPitchChange())) < 0.1) {
      if (++streak > 15) {
        fail(1, true);
      }
    } else {
      streak = 0;
    }
  }
}
