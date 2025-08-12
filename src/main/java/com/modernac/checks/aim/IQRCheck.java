package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class IQRCheck extends AimCheck {
    private final DebugLogger logger;
    public IQRCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "IQR", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement IQR detection
        logger.log(data.getUuid() + " handled IQR");
    }
}
