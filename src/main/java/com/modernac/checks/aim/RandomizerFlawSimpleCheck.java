package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class RandomizerFlawSimpleCheck extends AimCheck {
    private final DebugLogger logger;
    public RandomizerFlawSimpleCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Randomizer flaw Simple analysis", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Randomizer flaw Simple analysis detection
        logger.log(data.getUuid() + " handled Randomizer flaw Simple analysis");
    }
}
