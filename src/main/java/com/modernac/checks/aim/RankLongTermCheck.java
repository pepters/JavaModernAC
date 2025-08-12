package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class RankLongTermCheck extends AimCheck {
    private final DebugLogger logger;
    public RankLongTermCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Rank Long-term", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Rank Long-term detection
        logger.log(data.getUuid() + " handled Rank Long-term");
    }
}
