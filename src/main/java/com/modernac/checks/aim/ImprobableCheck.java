package com.modernac.checks.aim;

import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
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
        if (!(packet instanceof RotationData)) {
            return;
        }
        RotationData rot = (RotationData) packet;
        logger.log(data.getUuid() + " handled Improbable");
        if (Math.abs(rot.getYawChange()) > 200 || Math.abs(rot.getPitchChange()) > 200) {
            fail(1, true);
        }
    }
}
