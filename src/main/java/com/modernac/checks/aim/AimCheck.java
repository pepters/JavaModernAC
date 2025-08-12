package com.modernac.checks.aim;

import com.modernac.checks.Check;
import com.modernac.player.PlayerData;
import com.modernac.ModernACPlugin;

public abstract class AimCheck extends Check {
    public AimCheck(ModernACPlugin plugin, PlayerData data, String name, boolean experimental) {
        super(plugin, data, name, experimental);
    }
}
