package com.modernac.engine;

import com.modernac.ModernACPlugin;
import com.modernac.checks.Check;
import com.modernac.config.ConfigManager;
import com.modernac.engine.AlertEngine.AlertDetail;
import com.modernac.manager.PunishmentTier;
import com.modernac.util.LatencyGuard;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;

/** Aggregates evidence from independent checks and applies policy logic. */
public class DetectionEngine {
  private final ModernACPlugin plugin;
  private final Map<UUID, PlayerRecord> records = new ConcurrentHashMap<>();

  public DetectionEngine(ModernACPlugin plugin) {
    this.plugin = plugin;
  }

  private static final Set<String> NON_HEURISTIC =
      Set.of(
          "PerfectEntropy-LONG",
          "PerfectEntropy-VLONG",
          "AIM/Autocorr",
          "AIM/Continuity",
          "AIM/Quantization",
          "AIM/ClickCoupling",
          "AIM/Stickiness",
          "AIM/Outliers-SHORT",
          "LBGCD",
          "LBTween");

  public void record(Check check, int vl, boolean punishable, boolean experimental) {
    if (experimental && !plugin.getConfigManager().isExperimentalDetections()) {
      return;
    }
    double evidence = Math.min(1.0, Math.max(0.0, vl / 100.0));
    String family = check.getName();
    DetectionResult result = new DetectionResult(family, evidence, Window.SHORT, true, true, false);
    record(check, result);
  }

  public void record(Check check, DetectionResult result) {
    if (check.isExperimental() && !plugin.getConfigManager().isExperimentalDetections()) {
      return;
    }
    UUID uuid = check.getUuid();
    if (plugin.isExempt(uuid)) {
      return;
    }
    PlayerRecord record = records.computeIfAbsent(uuid, u -> new PlayerRecord());
    FamilyRecord fam = record.families.computeIfAbsent(result.getFamily(), f -> new FamilyRecord());
    double prev = fam.windowScores.getOrDefault(result.getWindow(), 0.0);
    fam.windowScores.put(result.getWindow(), Math.max(prev, result.getEvidenceScore()));
    fam.latencyOK &= result.isLatencyOK();
    fam.stabilityOK &= result.isStabilityOK();
    ConfigManager cfg = plugin.getConfigManager();
    PunishmentTier tier = cfg.getCheckTier(check.getName());
    if (tier.ordinal() < fam.tier.ordinal()) {
      fam.tier = tier;
    }
    com.modernac.player.PlayerData pd = plugin.getCheckManager().getPlayerData(uuid);
    int ping = pd != null ? pd.getCachedPing() : -1;
    double[] tpsArr = Bukkit.getTPS();
    double tps = tpsArr.length > 0 && Double.isFinite(tpsArr[0]) ? tpsArr[0] : 20.0;
      EvalOutcome outcome = evaluate(uuid, record, ping, tps);
      boolean stable =
          ping > 0
              && LatencyGuard.isStable(ping, tps, cfg.getUnstableConnectionLimit(), cfg.getTpsSoftGuard());
      boolean alertEligible =
          stable
              && outcome.families >= cfg.getMinIndependentFamiliesForAction()
              && (!cfg.isMultiWindowConfirmationRequired() || outcome.windows >= 2)
              && outcome.highest != null
              && (outcome.highest == PunishmentTier.HIGH
                  || outcome.highest == PunishmentTier.CRITICAL);
      if (!stable) {
        outcome.punish = false;
      }
    if (alertEligible) {
      double conf = outcome.highest == PunishmentTier.CRITICAL ? 1.0 : 0.9;
      AlertDetail detail =
          new AlertDetail(
              result.getFamily(),
              result.getWindow().name(),
              conf,
              ping,
              tps,
              outcome.highest,
              outcome.soft);
      plugin.getAlertEngine().enqueue(uuid, detail, outcome.highest == PunishmentTier.CRITICAL);
    }
    plugin.getMitigationManager().mitigate(uuid, outcome.reduction);
    if (outcome.punish) {
      plugin.getPunishmentManager().schedule(uuid, outcome.highest);
    } else {
      plugin.getPunishmentManager().cancel(uuid);
    }
  }

  private EvalOutcome evaluate(UUID uuid, PlayerRecord record, int ping, double tps) {
    ConfigManager cfg = plugin.getConfigManager();
    int requiredFamilies = cfg.getMinIndependentFamiliesForAction();
    boolean requireMultiWindow = cfg.isMultiWindowConfirmationRequired();
    int familyCount = 0;
    int familyCountNonHeu = 0;
    int windowCount = 0;
    double total = 0.0;
    PunishmentTier highest = null;
    for (Map.Entry<String, FamilyRecord> e : record.families.entrySet()) {
      String famName = e.getKey();
      FamilyRecord fam = e.getValue();
      double famScore =
          fam.windowScores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
      int famWindows = 0;
      for (double v : fam.windowScores.values()) {
        if (v >= 0.90) famWindows++;
      }
      if (fam.latencyOK && fam.stabilityOK && famScore >= 0.90) {
        familyCount++;
        if (NON_HEURISTIC.contains(famName)) {
          familyCountNonHeu++;
        }
        windowCount += famWindows;
        if (highest == null || fam.tier.ordinal() < highest.ordinal()) {
          highest = fam.tier;
        }
        total += famScore;
      }
    }
    double avg = familyCount > 0 ? total / familyCount : 0.0;
    double maxReduction = cfg.getMaxDamageReduction();
    if (!Double.isFinite(maxReduction) || maxReduction < 0) maxReduction = 0;
    if (maxReduction > 1) maxReduction = 1;
    int reduction = (int) Math.round(avg * maxReduction * 100);

    boolean soft = ping > cfg.getUnstableConnectionLimit() || tps < cfg.getTpsSoftGuard();
    if (soft && highest != null) {
      if (highest == PunishmentTier.CRITICAL) {
        highest = PunishmentTier.HIGH;
      } else if (highest == PunishmentTier.HIGH) {
        highest = PunishmentTier.MEDIUM;
      }
    }

    boolean punish =
        familyCountNonHeu >= requiredFamilies
            && (!requireMultiWindow || windowCount >= 2)
            && highest != null
            && highest != PunishmentTier.EXPERIMENTAL;
    record.currentTier = punish ? highest : null;
    return new EvalOutcome(soft, reduction, highest, punish, familyCountNonHeu, windowCount);
  }

  private static class EvalOutcome {
    final boolean soft;
    final int reduction;
    final PunishmentTier highest;
    boolean punish;
    final int families;
    final int windows;

    EvalOutcome(
        boolean soft,
        int reduction,
        PunishmentTier highest,
        boolean punish,
        int families,
        int windows) {
      this.soft = soft;
      this.reduction = reduction;
      this.highest = highest;
      this.punish = punish;
      this.families = families;
      this.windows = windows;
    }
  }

  public boolean isPunishable(UUID uuid, PunishmentTier tier) {
    PlayerRecord record = records.get(uuid);
    if (record == null || record.currentTier == null) return false;
    return record.currentTier.ordinal() <= tier.ordinal();
  }

  public void reset(UUID uuid) {
    records.remove(uuid);
    plugin.getPunishmentManager().cancel(uuid);
  }

  public void shutdown() {
    for (UUID uuid : records.keySet()) {
      plugin.getPunishmentManager().cancel(uuid);
    }
    records.clear();
  }

  public void reload() {
    for (UUID uuid : records.keySet()) {
      plugin.getPunishmentManager().cancel(uuid);
    }
    records.clear();
  }

  public DetectionSummary getSummary(UUID uuid) {
    PlayerRecord record = records.get(uuid);
    if (record == null) return null;
    double shortMax = 0.0;
    double longMax = 0.0;
    double veryLongMax = 0.0;
    for (FamilyRecord fam : record.families.values()) {
      shortMax = Math.max(shortMax, fam.windowScores.getOrDefault(Window.SHORT, 0.0));
      longMax = Math.max(longMax, fam.windowScores.getOrDefault(Window.LONG, 0.0));
      veryLongMax = Math.max(veryLongMax, fam.windowScores.getOrDefault(Window.VERY_LONG, 0.0));
    }
    int ping = -1;
    com.modernac.player.PlayerData pd = plugin.getCheckManager().getPlayerData(uuid);
    if (pd != null) {
      ping = pd.getCachedPing();
    }
    double[] tpsArr = org.bukkit.Bukkit.getTPS();
    double tps = tpsArr.length > 0 && Double.isFinite(tpsArr[0]) ? tpsArr[0] : 20.0;
    ConfigManager cfg = plugin.getConfigManager();
    boolean stable =
        ping > 0
            && LatencyGuard.isStable(
                ping, tps, cfg.getUnstableConnectionLimit(), cfg.getTpsSoftGuard());
    return new DetectionSummary(stable, stable, shortMax, longMax, veryLongMax);
  }

  public static class DetectionSummary {
    public final boolean latencyOK;
    public final boolean stabilityOK;
    public final double shortWindow;
    public final double longWindow;
    public final double veryLongWindow;

    public DetectionSummary(
        boolean latencyOK,
        boolean stabilityOK,
        double shortWindow,
        double longWindow,
        double veryLongWindow) {
      this.latencyOK = latencyOK;
      this.stabilityOK = stabilityOK;
      this.shortWindow = shortWindow;
      this.longWindow = longWindow;
      this.veryLongWindow = veryLongWindow;
    }
  }

  private static class PlayerRecord {
    final Map<String, FamilyRecord> families = new ConcurrentHashMap<>();
    PunishmentTier currentTier;
  }

  private static class FamilyRecord {
    final Map<Window, Double> windowScores = new EnumMap<>(Window.class);
    boolean latencyOK = true;
    boolean stabilityOK = true;
    PunishmentTier tier = PunishmentTier.MEDIUM;
  }
}
