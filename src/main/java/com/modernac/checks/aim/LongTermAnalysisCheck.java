package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class LongTermAnalysisCheck extends AimCheck {
    private final DebugLogger logger;
    public LongTermAnalysisCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Long-term Analysis", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Long-term Analysis detection
        logger.log(data.getUuid() + " handled Long-term Analysis");
    }
}
