package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class ImprobableCheck extends AimCheck {
    private final DebugLogger logger;
    public ImprobableCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Improbable", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Improbable detection
        logger.log(data.getUuid() + " handled Improbable");
    }
}
