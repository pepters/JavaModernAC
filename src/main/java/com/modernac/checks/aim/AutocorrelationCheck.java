package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.engine.DetectionResult;
import com.modernac.engine.Window;
import com.modernac.net.LagCompensator;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.util.MathUtil;
import java.util.ArrayDeque;
import java.util.Deque;
import org.bukkit.Bukkit;

/** Autocorrelation of yaw deltas over long window. */
public class AutocorrelationCheck extends AimCheck {
  private static final String FAMILY = "AIM/Autocorr";

  private static final int MIN = 64;
  private static final double STD_MIN = 0.03;
  private static final int[] LAGS = {2, 3, 4, 5, 6};
  private static final double HIGH = 0.85;
  private static final double CRIT = 0.92;
  private static final int HITS_REQ = 2;
  private static final long COOLDOWN_MS = 1500L;

  private final Deque<Double> window = new ArrayDeque<>();
  private int hits;
  private long lastFail;

  public AutocorrelationCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "Autocorrelation", false);
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
    LagCompensator.LagContext ctx = plugin.getLagCompensator().estimate(data.getUuid());
    synchronized (window) {
      if (window.size() >= 256) {
        window.pollFirst();
      }
      window.addLast(yaw);
    }
    double[] arr = MathUtil.snapshotNonNull(window);
    if (arr.length < MIN) {
      return;
    }
    if (stddev(arr) < STD_MIN) {
      return;
    }
    double maxAbsR = 0.0;
    for (int k : LAGS) {
      maxAbsR = Math.max(maxAbsR, Math.abs(autocorr(arr, k)));
    }
    long now = System.currentTimeMillis();
    if (now - lastFail < COOLDOWN_MS) {
      return;
    }
    double env = clamp(1 - ctx.jitterMs / 200.0, 0.6, 1.0);
    if (maxAbsR >= CRIT) {
      hits++;
      if (hits >= HITS_REQ) {
        hits = 0;
        lastFail = now;
        fail(new DetectionResult(FAMILY, 1.0 * env, Window.LONG, true, true, true));
      }
    } else if (maxAbsR >= HIGH) {
      hits++;
      if (hits >= HITS_REQ) {
        hits = 0;
        lastFail = now;
        fail(new DetectionResult(FAMILY, 0.9 * env, Window.LONG, true, true, true));
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

  private static double autocorr(double[] x, int lag) {
    int n = x.length;
    if (lag >= n) return 0.0;
    double sum = 0.0;
    for (double v : x) sum += v;
    double mean = sum / n;
    double num = 0.0;
    double den = 0.0;
    for (int i = 0; i < n; i++) {
      double d = x[i] - mean;
      den += d * d;
    }
    if (den == 0.0) return 0.0;
    for (int i = lag; i < n; i++) {
      num += (x[i] - mean) * (x[i - lag] - mean);
    }
    return num / den;
  }

  private static double clamp(double v, double lo, double hi) {
    return Math.max(lo, Math.min(hi, v));
  }
}
