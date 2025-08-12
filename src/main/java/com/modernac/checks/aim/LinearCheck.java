package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class LinearCheck extends AimCheck {
    private final DebugLogger logger;
    public LinearCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Linear", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Linear detection
        logger.log(data.getUuid() + " handled Linear");
    }
}
