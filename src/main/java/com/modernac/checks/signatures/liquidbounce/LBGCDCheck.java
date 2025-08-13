package com.modernac.checks.signatures.liquidbounce;

import com.modernac.ModernACPlugin;
import com.modernac.checks.aim.AimCheck;
import com.modernac.engine.DetectionResult;
import com.modernac.engine.Window;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.util.MathUtil;
import java.util.ArrayDeque;
import java.util.Deque;

/** Detects LiquidBounce's stable rotation quantization via GCD analysis. */
public class LBGCDCheck extends AimCheck {
  private static final double SCALE = 1_000_000.0;
  private final Deque<Integer> buffer = new ArrayDeque<>();

  private static int[] snapshotInt(Deque<Integer> q) {
    Integer[] tmp;
    synchronized (q) {
      tmp = q.toArray(new Integer[0]);
    }
    int[] out = new int[tmp.length];
    int n = 0;
    for (Integer it : tmp) {
      if (it != null) out[n++] = it;
    }
    return n == out.length ? out : java.util.Arrays.copyOf(out, n);
  }

  public LBGCDCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "LB_GCD", false);
  }

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) return;
    RotationData rot = (RotationData) packet;
    double yaw = Math.abs(rot.getYawChange());
    if (!Double.isFinite(yaw)) {
      return;
    }
    int quant = (int) Math.round(yaw * SCALE);
    synchronized (buffer) {
      if (buffer.size() >= 40) {
        buffer.pollFirst();
      }
      buffer.addLast(quant);
    }
    int[] diffs = snapshotInt(buffer);
    if (diffs.length < 40) {
      return;
    }
    int gcd = MathUtil.findGcd(diffs);
    double[] arr = new double[diffs.length];
    for (int i = 0; i < diffs.length; i++) {
      arr[i] = diffs[i] / SCALE;
    }
    double iqr = MathUtil.iqr(arr);
    if (gcd > 0 && iqr < 0.05) {
      DetectionResult result = new DetectionResult("GCD", 1.0, Window.LONG, true, true, true);
      fail(result);
    }
    synchronized (buffer) {
      buffer.clear();
    }
  }
}
