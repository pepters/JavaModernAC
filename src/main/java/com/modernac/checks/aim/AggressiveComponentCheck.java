package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class AggressiveComponentCheck extends AimCheck {
    private final DebugLogger logger;
    public AggressiveComponentCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Aggressive Component", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        double threshold = 5 * plugin.getConfigManager().getCombatTolerance().getMultiplier();
        logger.log(data.getUuid() + " handled Aggressive Component");
        if (Math.random() * 10 > threshold) {
            fail(1, true);
        }
    }
}
