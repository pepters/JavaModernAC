package com.modernac.checks.combat;

import com.modernac.ModernACPlugin;
import com.modernac.checks.Check;
import com.modernac.engine.DetectionResult;
import com.modernac.engine.Window;
import com.modernac.player.PlayerData;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** Experimental detection for auto totem usage. */
public class AutoTotemDetection extends Check {
  public static final class CriticalDamageEvent {
    public final boolean lineOfSight;

    public CriticalDamageEvent(boolean lineOfSight) {
      this.lineOfSight = lineOfSight;
    }
  }

  public static final class OffhandSwapEvent {}

  public static final class TotemPopEvent {}

  private long lastCritical;
  private boolean lastLos;
  private long lastSwap;
  private long lastReaction;

  private final double[] shortBuf = new double[25];
  private final double[] longBuf = new double[100];
  private final double[] veryLongBuf = new double[600];
  private int shortIdx, longIdx, veryLongIdx;
  private int shortCount, longCount, veryLongCount;
  private double shortSum, longSum, veryLongSum;

  public AutoTotemDetection(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "AutoTotemDetection", true);
  }

  @Override
  public void handle(Object packet) {
    long now = System.currentTimeMillis();
    if (packet instanceof CriticalDamageEvent) {
      lastCritical = now;
      lastLos = ((CriticalDamageEvent) packet).lineOfSight;
    } else if (packet instanceof OffhandSwapEvent) {
      if (lastCritical > 0) {
        lastSwap = now;
        lastReaction = now - lastCritical;
      }
    } else if (packet instanceof TotemPopEvent) {
      if (lastSwap > 0 && lastCritical > 0) {
        evaluate();
      }
    }
  }

  private void evaluate() {
    long reaction = lastReaction;
    lastCritical = 0;
    lastSwap = 0;
    Player player = Bukkit.getPlayer(getUuid());
    if (player == null) {
      return;
    }
    int ping = player.getPing();
    double tps = 20.0;
    double[] tpsArr = Bukkit.getTPS();
    if (tpsArr.length > 0 && Double.isFinite(tpsArr[0])) {
      tps = tpsArr[0];
    }
    if (ping > 180 || tps < 18.0 || !lastLos) {
      trace(
          "gate-fail ping="
              + ping
              + " tps="
              + String.format(Locale.US, "%.1f", tps)
              + " los="
              + lastLos);
      return;
    }
    double suspicion;
    if (reaction <= 50) {
      suspicion = 1.0;
    } else if (reaction <= 100) {
      suspicion = 0.8;
    } else if (reaction <= 150) {
      suspicion = 0.4;
    } else {
      suspicion = 0.0;
    }
    if (suspicion <= 0.0) {
      trace("reaction=" + reaction + "ms ignored");
      return;
    }
    addEvidence(suspicion);
    double shortAvg = shortSum / (shortCount == 0 ? 1 : shortCount);
    double longAvg = longSum / (longCount == 0 ? 1 : longCount);
    double veryLongAvg = veryLongSum / (veryLongCount == 0 ? 1 : veryLongCount);
    double e = 0.6 * shortAvg + 0.3 * longAvg + 0.1 * veryLongAvg;
    trace(
        "reaction="
            + reaction
            + "ms s="
            + String.format(Locale.US, "%.2f", suspicion)
            + " E="
            + String.format(Locale.US, "%.2f", e));
    if (e >= 0.75) {
      DetectionResult result = new DetectionResult("AUTOTOTEM", e, Window.SHORT, true, true, true);
      fail(result);
    }
  }

  private void addEvidence(double s) {
    shortSum -= shortBuf[shortIdx];
    shortBuf[shortIdx] = s;
    shortSum += s;
    if (shortCount < shortBuf.length) shortCount++;
    shortIdx = (shortIdx + 1) % shortBuf.length;

    longSum -= longBuf[longIdx];
    longBuf[longIdx] = s;
    longSum += s;
    if (longCount < longBuf.length) longCount++;
    longIdx = (longIdx + 1) % longBuf.length;

    veryLongSum -= veryLongBuf[veryLongIdx];
    veryLongBuf[veryLongIdx] = s;
    veryLongSum += s;
    if (veryLongCount < veryLongBuf.length) veryLongCount++;
    veryLongIdx = (veryLongIdx + 1) % veryLongBuf.length;
  }
}
