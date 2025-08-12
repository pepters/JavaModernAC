package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class SnapRandomizerComponentCheck extends AimCheck {
    private final DebugLogger logger;
    public SnapRandomizerComponentCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Snap/Randomizer Component", false);
        this.logger = plugin.getDebugLogger();
    }

    @Override
    public void handle(Object packet) {
        // TODO: Implement Snap/Randomizer Component detection
        logger.log(data.getUuid() + " handled Snap/Randomizer Component");
    }
}
