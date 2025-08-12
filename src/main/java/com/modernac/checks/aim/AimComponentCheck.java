package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class AimComponentCheck extends AimCheck {
    private final DebugLogger logger;
    public AimComponentCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Aim Component", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Aim Component detection
        logger.log(data.getUuid() + " handled Aim Component");
    }
}
