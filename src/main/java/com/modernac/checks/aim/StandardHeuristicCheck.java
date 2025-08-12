package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class StandardHeuristicCheck extends AimCheck {
    private final DebugLogger logger;
    public StandardHeuristicCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Standard heuristic", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Standard heuristic detection
        logger.log(data.getUuid() + " handled Standard heuristic");
    }
}
