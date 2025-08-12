package com.modernac.checks.signatures;

import com.modernac.ModernACPlugin;
import com.modernac.checks.Check;
import com.modernac.checks.signatures.liquidbounce.LBTweenCheck;
import com.modernac.checks.signatures.nursultan.NRSnapTailCheck;
import com.modernac.player.PlayerData;

import java.util.Arrays;
import java.util.List;

/**
 * Factory for client signature based checks.
 */
public class SignatureCheckFactory {
    public static List<Check> build(ModernACPlugin plugin, PlayerData data) {
        return Arrays.asList(
                new LBTweenCheck(plugin, data),
                new NRSnapTailCheck(plugin, data)
        );
    }
}

