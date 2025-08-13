package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

public class SnapRandomizerComponentCheck extends AimCheck {

  public SnapRandomizerComponentCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Snap/Randomizer Component", false);
  }

  private double lastYaw, lastPitch;

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    trace("handled Snap/Randomizer Component");
    boolean small = Math.abs(lastYaw) < 1 && Math.abs(lastPitch) < 1;
    boolean big = Math.abs(rot.getYawChange()) > 50 || Math.abs(rot.getPitchChange()) > 50;
    if (small && big) {
      fail(1, true);
    }
    lastYaw = rot.getYawChange();
    lastPitch = rot.getPitchChange();
  }
}
