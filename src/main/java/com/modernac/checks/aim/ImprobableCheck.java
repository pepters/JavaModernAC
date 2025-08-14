package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class ImprobableCheck extends AimCheck {

  public ImprobableCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Improbable", true);
  }

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    trace("handled Improbable");
    if (Math.abs(rot.getYawChange()) > 200 || Math.abs(rot.getPitchChange()) > 200) {
      fail(1, true);
    }
  }
}
