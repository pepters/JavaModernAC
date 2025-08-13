package com.modernac.checks.misc;

import com.modernac.ModernACPlugin;
import com.modernac.checks.Check;
import com.modernac.player.PlayerData;

public class AutoTotemCheck extends Check {
  public AutoTotemCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "AutoTotem", true);
  }

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof String)) {
      return;
    }
    if ("TOTEM_USE".equals(packet)) {
      fail(1, false);
    }
  }
}
