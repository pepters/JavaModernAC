package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.logging.DebugLogger;
import com.modernac.ModernACPlugin;

public class RandomizerFlawSimpleCheck extends AimCheck {
    private final DebugLogger logger;
    public RandomizerFlawSimpleCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "Randomizer flaw Simple analysis", false);
        this.logger = plugin.getDebugLogger();
    }

    private int streak;

    @Override
    public void handle(Object packet) {
        if (!(packet instanceof RotationData)) {
            return;
        }
        RotationData rot = (RotationData) packet;
        logger.log(data.getUuid() + " handled Randomizer flaw Simple analysis");
        double mod = Math.abs(rot.getYawChange()) % 0.1;
        if (mod < 1e-6) {
            if (++streak > 3) {
                fail(1, true);
            }
        } else {
            streak = 0;
        }
    }
}
