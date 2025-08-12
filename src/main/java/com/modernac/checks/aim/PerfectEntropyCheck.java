package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class PerfectEntropyCheck extends AimCheck {
    private final DebugLogger logger;
    public PerfectEntropyCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Perfect/Similar shannon entropy", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Perfect/Similar shannon entropy detection
        logger.log(data.getUuid() + " handled Perfect/Similar shannon entropy");
    }
}
