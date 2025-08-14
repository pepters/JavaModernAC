package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AimCheckFactory {
  public static List<AimCheck> build(ModernACPlugin plugin, PlayerData data) {
    return new ArrayList<>(
        Arrays.asList(
            new SimpleHeuristicCheck(plugin, data),
            new AggressiveComponentCheck(plugin, data),
            new RandomizerFlawHeuristicCheck(plugin, data),
            new SnapRandomizerComponentCheck(plugin, data),
            new ImprobableCheck(plugin, data),
            new PerfectEntropyCheck(plugin, data),
            new IQRCheck(plugin, data),
            new ZFactorCheck(plugin, data),
            new RankCheck(plugin, data),
            new PatternAnalysisCheck(plugin, data),
            new PatternStatisticsCheck(plugin, data),
            new DistinctCheck(plugin, data),
            new RankLongTermCheck(plugin, data)));
  }
}
