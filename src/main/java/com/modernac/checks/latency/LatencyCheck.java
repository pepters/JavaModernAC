package com.modernac.checks.latency;

import com.modernac.ModernACPlugin;
import com.modernac.checks.Check;
import com.modernac.player.PlayerData;

public abstract class LatencyCheck extends Check {
    public LatencyCheck(ModernACPlugin plugin, PlayerData data, String name) {
        super(plugin, data, name, false);
    }
}
