package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class RandomizerFlawHeuristicCheck extends AimCheck {
    private final DebugLogger logger;
    public RandomizerFlawHeuristicCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Randomizer flaw Heuristic", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Randomizer flaw Heuristic detection
        logger.log(data.getUuid() + " handled Randomizer flaw Heuristic");
    }
}
