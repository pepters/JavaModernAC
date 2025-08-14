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

/** Detects quantized yaw changes over long windows. */
public class QuantizationLongCheck extends AimCheck {
  private static final String FAMILY = "AIM/Quantization";

  private static final double SCALE = 1_000_000.0;
  private static final int MIN_LONG = 64;
  private static final int MIN_VLONG = 256;
  private static final double IQR_NORM_MAX = 0.05;
  private static final int HITS_REQ = 2;
  private static final long COOLDOWN_MS = 1500L;

  private final Deque<Double> longWindow = new ArrayDeque<>();
  private final Deque<Double> vlongWindow = new ArrayDeque<>();
  private int hitsLong;
  private int hitsVLong;
  private long lastFailLong;
  private long lastFailVLong;

  public QuantizationLongCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "QuantizationLong", false);
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
      if (longWindow.size() >= 256) longWindow.pollFirst();
      longWindow.addLast(yaw);
    }
    synchronized (vlongWindow) {
      if (vlongWindow.size() >= 512) vlongWindow.pollFirst();
      vlongWindow.addLast(yaw);
    }
    analyze(false, ctx);
    analyze(true, ctx);
  }

  private void analyze(boolean vlong, LagCompensator.LagContext ctx) {
    Deque<Double> q = vlong ? vlongWindow : longWindow;
    double[] a = MathUtil.snapshotNonNull(q);
    int min = vlong ? MIN_VLONG : MIN_LONG;
    if (a.length < min) {
      return;
    }
    int[] qint = quantizeAbs(a, SCALE);
    int gcd = MathUtil.findGcd(qint);
    double iqr = MathUtil.iqr(a);
    long now = System.currentTimeMillis();
    if (gcd > 0 && iqr < IQR_NORM_MAX) {
      double env = clamp(1 - ctx.jitterMs / 200.0, 0.6, 1.0);
      if (vlong) {
        if (now - lastFailVLong < COOLDOWN_MS) return;
        hitsVLong++;
        if (hitsVLong >= HITS_REQ) {
          hitsVLong = 0;
          lastFailVLong = now;
          fail(new DetectionResult(FAMILY, 1.0 * env, Window.VERY_LONG, true, true, true));
        }
      } else {
        if (now - lastFailLong < COOLDOWN_MS) return;
        hitsLong++;
        if (hitsLong >= HITS_REQ) {
          hitsLong = 0;
          lastFailLong = now;
          fail(new DetectionResult(FAMILY, 0.9 * env, Window.LONG, true, true, true));
        }
      }
    } else {
      if (vlong) {
        hitsVLong = 0;
      } else {
        hitsLong = 0;
      }
    }
  }

  private static int[] quantizeAbs(double[] a, double scale) {
    int[] out = new int[a.length];
    for (int i = 0; i < a.length; i++) {
      out[i] = (int) Math.round(Math.abs(a[i] * scale));
    }
    return out;
  }

  private static double clamp(double v, double lo, double hi) {
    return Math.max(lo, Math.min(hi, v));
  }
}
