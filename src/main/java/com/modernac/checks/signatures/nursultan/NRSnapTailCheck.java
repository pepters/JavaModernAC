package com.modernac.checks.signatures.nursultan;

import com.modernac.ModernACPlugin;
import com.modernac.checks.aim.AimCheck;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;

/**
 * Detects Nursultan client snap then smooth tail behaviour.
 */
public class NRSnapTailCheck extends AimCheck {
    private boolean tracking = false;
    private int ticks = 0;

    public NRSnapTailCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "NR-SnapTail", false);
    }

    @Override
    public void handle(Object packet) {
        if (!(packet instanceof RotationData)) return;
        RotationData rot = (RotationData) packet;
        double yaw = rot.getYawChange();
        if (!tracking) {
            if (yaw > 45.0) {
                tracking = true;
                ticks = 0;
            }
            return;
        }
        ticks++;
        if (yaw < 0.5) {
            if (ticks >= 3 && ticks <= 6) {
                fail(10, true);
                tracking = false;
            }
        } else {
            tracking = false;
        }
        if (ticks > 6) {
            tracking = false;
        }
    }
}

