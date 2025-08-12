package com.modernac.checks.latency;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;

import java.util.ArrayList;
import java.util.List;

public class LatencyCheckFactory {
    public static List<LatencyCheck> build(ModernACPlugin plugin, PlayerData data) {
        List<LatencyCheck> list = new ArrayList<>();
        list.add(new BlinkCheck(plugin, data));
        list.add(new LagrangeCheck(plugin, data));
        return list;
    }
}
