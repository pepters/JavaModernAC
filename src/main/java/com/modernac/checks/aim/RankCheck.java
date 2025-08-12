package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class RankCheck extends AimCheck {
    private final DebugLogger logger;
    public RankCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Rank", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Rank detection
        logger.log(data.getUuid() + " handled Rank");
    }
}
