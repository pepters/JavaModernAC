package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.engine.DetectionResult;
import com.modernac.engine.Window;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.util.MathUtil;
import java.util.ArrayDeque;
import java.util.Deque;
import org.bukkit.Bukkit;

/** Measures continuity of yaw curvature over a long window. */
public class CurvatureContinuityCheck extends AimCheck {
  private static final String FAMILY = "AIM/Continuity";

  private static final int MIN = 80;
  private static final double STD_MIN = 0.04;
  private static final double VAR_D2_HIGH = 0.0025;
  private static final double VAR_D2_CRIT = 0.0016;
  private static final double SIGN_SWITCH_RATE_MAX = 0.08;
  private static final int HITS_REQ = 3;
  private static final long COOLDOWN_MS = 2000L;

  private final Deque<Double> window = new ArrayDeque<>();
  private int hits;
  private long lastFail;

  public CurvatureContinuityCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "CurvatureContinuity", false);
  }

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    double yaw = rot.getYawChange();
    if (!Double.isFinite(yaw)) {
      return;
    }
    int ping = data.getCachedPing();
    double[] tpsArr = Bukkit.getTPS();
    double tps = tpsArr.length > 0 && Double.isFinite(tpsArr[0]) ? tpsArr[0] : 20.0;
    if (ping <= 0 || ping > 180 || tps < 18.0) {
      return;
    }
    synchronized (window) {
      if (window.size() >= 256) {
        window.pollFirst();
      }
      window.addLast(yaw);
    }
    double[] d = MathUtil.snapshotNonNull(window);
    if (d.length < MIN) {
      return;
    }
    if (stddev(d) < STD_MIN) {
      return;
    }
    double[] d2 = secondDiffs(d);
    double var = variance(d2);
    double signRate = signSwitchRate(d);
    long now = System.currentTimeMillis();
    if (signRate < SIGN_SWITCH_RATE_MAX) {
      if (now - lastFail < COOLDOWN_MS) {
        return;
      }
      if (var <= VAR_D2_CRIT) {
        hits++;
        if (hits >= HITS_REQ) {
          hits = 0;
          lastFail = now;
          fail(new DetectionResult(FAMILY, 1.0, Window.LONG, true, true, true));
        }
      } else if (var <= VAR_D2_HIGH) {
        hits++;
        if (hits >= HITS_REQ) {
          hits = 0;
          lastFail = now;
          fail(new DetectionResult(FAMILY, 0.9, Window.LONG, true, true, true));
        }
      } else {
        hits = 0;
      }
    } else {
      hits = 0;
    }
  }

  private static double stddev(double[] a) {
    int n = a.length;
    double sum = 0.0;
    for (double v : a) sum += v;
    double mean = sum / n;
    double var = 0.0;
    for (double v : a) {
      double d = v - mean;
      var += d * d;
    }
    return Math.sqrt(var / n);
  }

  private static double[] secondDiffs(double[] d) {
    if (d.length < 3) return new double[0];
    double[] out = new double[d.length - 2];
    for (int i = 0; i < d.length - 2; i++) {
      out[i] = d[i + 2] - 2 * d[i + 1] + d[i];
    }
    return out;
  }

  private static double variance(double[] a) {
    if (a.length == 0) return 0.0;
    double sum = 0.0;
    for (double v : a) sum += v;
    double mean = sum / a.length;
    double var = 0.0;
    for (double v : a) {
      double d = v - mean;
      var += d * d;
    }
    return var / a.length;
  }

  private static double signSwitchRate(double[] d) {
    int switches = 0;
    int prevSign = 0;
    for (double v : d) {
      int sign = v > 0 ? 1 : (v < 0 ? -1 : 0);
      if (sign != 0 && prevSign != 0 && sign != prevSign) {
        switches++;
      }
      if (sign != 0) {
        prevSign = sign;
      }
    }
    return d.length > 1 ? switches / (double) (d.length - 1) : 0.0;
  }
}
