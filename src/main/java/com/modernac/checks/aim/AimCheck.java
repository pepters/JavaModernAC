package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.checks.Check;
import com.modernac.player.PlayerData;

public abstract class AimCheck extends Check {
  public AimCheck(ModernACPlugin plugin, PlayerData data, String name, boolean experimental) {
    super(plugin, data, name, experimental);
  }
}
