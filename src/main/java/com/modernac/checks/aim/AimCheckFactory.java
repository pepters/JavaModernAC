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
            new PerfectEntropyCheck(plugin, data),
            new IQRCheck(plugin, data),
            new ZFactorCheck(plugin, data),
            new RankCheck(plugin, data),
            new PatternAnalysisCheck(plugin, data),
            new PatternStatisticsCheck(plugin, data),
            new DistinctCheck(plugin, data),
            new RankLongTermCheck(plugin, data),
            new LongTermEntropyCheck(plugin, data),
            new AutocorrelationCheck(plugin, data),
            new CurvatureContinuityCheck(plugin, data),
            new QuantizationLongCheck(plugin, data),
            new TargetStickinessLongCheck(plugin, data)));
  }
}
