package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AimCheckFactory {
    public static List<AimCheck> build(ModernACPlugin plugin, PlayerData data) {
        return new ArrayList<>(Arrays.asList(
                new SimpleHeuristicCheck(plugin, data),
                new AimComponentCheck(plugin, data),
                new SyncComponentCheck(plugin, data),
                new ConstantComponentCheck(plugin, data),
                new AggressiveComponentCheck(plugin, data),
                new SnapRandomizerComponentCheck(plugin, data),
                new InterpolationFlawCheck(plugin, data),
                new ConstantRotationsOneCheck(plugin, data),
                new ConstantRotationsTwoCheck(plugin, data),
                new ConstantRotationsThreeCheck(plugin, data),
                new InconsistentLowCheck(plugin, data),
                new InconsistentZeroCheck(plugin, data),
                new PerfectEntropyCheck(plugin, data),
                new StandardHeuristicCheck(plugin, data),
                new RandomizerFlawHeuristicCheck(plugin, data),
                new RandomizerFlawSimpleCheck(plugin, data),
                new InvalidPitchCheck(plugin, data),
                new DistinctCheck(plugin, data),
                new SmoothCheck(plugin, data),
                new FactorCheck(plugin, data),
                new StatisticsCheck(plugin, data),
                new PatternStatisticsCheck(plugin, data),
                new IQRCheck(plugin, data),
                new ZFactorCheck(plugin, data),
                new ImprobableCheck(plugin, data),
                new AnalysisCheck(plugin, data),
                new LinearCheck(plugin, data),
                new RankCheck(plugin, data),
                new PatternAnalysisCheck(plugin, data),
                new LongTermAnalysisCheck(plugin, data),
                new RankLongTermCheck(plugin, data)
        ));
    }
}
