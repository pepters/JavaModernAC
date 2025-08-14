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

/** Long/very-long term entropy analysis of yaw deltas. */
public class LongTermEntropyCheck extends AimCheck {
  private static final String FAMILY_LONG = "PerfectEntropy-LONG";
  private static final String FAMILY_VLONG = "PerfectEntropy-VLONG";

  private static final int MIN_LONG = 64;
  private static final int MIN_VLONG = 256;
  private static final double STD_MIN = 0.03;
  private static final int BINS = 64;
  private static final double H_LONG_HIGH = 2.0;
  private static final double H_LONG_CRIT = 1.2;
  private static final double H_VLONG_HIGH = 2.2;
  private static final double H_VLONG_CRIT = 1.4;
  private static final int HITS_REQ = 2;
  private static final long COOLDOWN_MS = 1500L;

  private final Deque<Double> longWindow = new ArrayDeque<>();
  private final Deque<Double> vlongWindow = new ArrayDeque<>();

  private int hitsLong;
  private int hitsVLong;
  private long lastFailLong;
  private long lastFailVLong;

  public LongTermEntropyCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "LongTermEntropy", false);
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
    synchronized (longWindow) {
      if (longWindow.size() >= 128) {
        longWindow.pollFirst();
      }
      longWindow.addLast(yaw);
    }
    synchronized (vlongWindow) {
      if (vlongWindow.size() >= 512) {
        vlongWindow.pollFirst();
      }
      vlongWindow.addLast(yaw);
    }
    analyze(Window.LONG, ctx);
    analyze(Window.VERY_LONG, ctx);
  }

  private void analyze(Window window, LagCompensator.LagContext ctx) {
    Deque<Double> q = window == Window.LONG ? longWindow : vlongWindow;
    double[] arr = MathUtil.snapshotNonNull(q);
    int min = window == Window.LONG ? MIN_LONG : MIN_VLONG;
    if (arr.length < min) {
      return;
    }
    double std = stddev(arr);
    if (std < STD_MIN) {
      return;
    }
    double h = shannonEntropyAbsYaw(arr, BINS);
    long now = System.currentTimeMillis();
    if (window == Window.LONG) {
      if (now - lastFailLong < COOLDOWN_MS) {
        return;
      }
      if (h < H_LONG_CRIT) {
        hitsLong++;
        if (hitsLong >= HITS_REQ) {
          hitsLong = 0;
          lastFailLong = now;
          double e = 1.0 * clamp(1 - ctx.jitterMs / 200.0, 0.6, 1.0);
          fail(new DetectionResult(FAMILY_LONG, e, Window.LONG, true, true, true));
        }
      } else if (h < H_LONG_HIGH) {
        hitsLong++;
        if (hitsLong >= HITS_REQ) {
          hitsLong = 0;
          lastFailLong = now;
          double e = 0.9 * clamp(1 - ctx.jitterMs / 200.0, 0.6, 1.0);
          fail(new DetectionResult(FAMILY_LONG, e, Window.LONG, true, true, true));
        }
      } else {
        hitsLong = 0;
      }
    } else {
      if (now - lastFailVLong < COOLDOWN_MS) {
        return;
      }
      if (h < H_VLONG_CRIT) {
        hitsVLong++;
        if (hitsVLong >= HITS_REQ) {
          hitsVLong = 0;
          lastFailVLong = now;
          double e = 1.0 * clamp(1 - ctx.jitterMs / 200.0, 0.6, 1.0);
          fail(new DetectionResult(FAMILY_VLONG, e, Window.VERY_LONG, true, true, true));
        }
      } else if (h < H_VLONG_HIGH) {
        hitsVLong++;
        if (hitsVLong >= HITS_REQ) {
          hitsVLong = 0;
          lastFailVLong = now;
          double e = 0.9 * clamp(1 - ctx.jitterMs / 200.0, 0.6, 1.0);
          fail(new DetectionResult(FAMILY_VLONG, e, Window.VERY_LONG, true, true, true));
        }
      } else {
        hitsVLong = 0;
      }
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

  private static double shannonEntropyAbsYaw(double[] a, int bins) {
    double max = 0.0;
    for (double v : a) {
      double av = Math.abs(v);
      if (av > max) max = av;
    }
    if (max <= 0.0) {
      return 0.0;
    }
    double size = max / bins;
    int[] counts = new int[bins];
    for (double v : a) {
      int idx = (int) (Math.abs(v) / size);
      if (idx >= bins) idx = bins - 1;
      counts[idx]++;
    }
    double h = 0.0;
    for (int c : counts) {
      if (c > 0) {
        double p = (double) c / a.length;
        h -= p * (Math.log(p) / Math.log(2));
      }
    }
    return h;
  }

  private static double clamp(double v, double lo, double hi) {
    return Math.max(lo, Math.min(hi, v));
  }
}
