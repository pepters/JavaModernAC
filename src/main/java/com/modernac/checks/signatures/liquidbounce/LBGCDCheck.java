package com.modernac.checks.signatures.liquidbounce;

import com.modernac.ModernACPlugin;
import com.modernac.checks.aim.AimCheck;
import com.modernac.engine.DetectionResult;
import com.modernac.engine.Window;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.util.MathUtil;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Detects LiquidBounce's stable rotation quantization via GCD analysis.
 */
public class LBGCDCheck extends AimCheck {
    private final Deque<Double> buffer = new ArrayDeque<>();

    public LBGCDCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "LB_GCD", false);
    }

    @Override
    public void handle(Object packet) {
        if (!(packet instanceof RotationData)) return;
        RotationData rot = (RotationData) packet;
        buffer.add(Math.abs(rot.getYawChange()));
        if (buffer.size() == 40) {
            double gcd = MathUtil.findGcd(buffer);
            double iqr = MathUtil.iqr(buffer);
            if (gcd > 0.0 && iqr < 0.05) {
                DetectionResult result = new DetectionResult("GCD", 1.0, Window.LONG, true, true, true);
                fail(result);
            }
            buffer.clear();
        }
    }
}
