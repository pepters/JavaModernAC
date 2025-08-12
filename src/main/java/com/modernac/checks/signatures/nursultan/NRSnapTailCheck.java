package com.modernac.checks.signatures.nursultan;

import com.modernac.ModernACPlugin;
import com.modernac.checks.aim.AimCheck;
import com.modernac.engine.DetectionResult;
import com.modernac.engine.Window;
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
                DetectionResult result = new DetectionResult("GEOMETRY", 1.0, Window.SHORT, true, true, true);
                fail(result);
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

