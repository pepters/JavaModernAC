package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class SimpleHeuristicCheck extends AimCheck {
    private final DebugLogger logger;
    public SimpleHeuristicCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Simple heuristic", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        double threshold = 10 * plugin.getConfigManager().getCombatTolerance().getMultiplier();
        if (Math.random() * 10 > threshold) {
            fail(1, true);
        }
    }
}
