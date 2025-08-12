package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class SyncComponentCheck extends AimCheck {
    private final DebugLogger logger;
    public SyncComponentCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Sync Component", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Sync Component detection
        logger.log(data.getUuid() + " handled Sync Component");
    }
}
