package com.modernac.util;

import java.util.Arrays;
import java.util.Deque;

public final class MathUtil {
  private MathUtil() {}

  public static int findGcd(int[] a) {
    if (a == null || a.length == 0) return 0;
    int g = Math.abs(a[0]);
    for (int i = 1; i < a.length && g != 1; i++) {
      int x = Math.abs(a[i]);
      while (x != 0) {
        int t = g % x;
        g = x;
        x = t;
      }
    }
    return g;
  }

  public static double iqr(double[] values) {
    if (values == null || values.length == 0) return 0.0;
    double[] copy = Arrays.copyOf(values, values.length);
    Arrays.sort(copy);
    int n = copy.length;
    double q1 = copy[n / 4];
    double q3 = copy[3 * n / 4];
    return q3 - q1;
  }

  public static double[] snapshotNonNull(Deque<Double> q) {
    Double[] tmp;
    synchronized (q) {
      tmp = q.toArray(new Double[0]);
    }
    double[] out = new double[tmp.length];
    int n = 0;
    for (Double d : tmp) {
      if (d != null) {
        double v = d.doubleValue();
        if (!Double.isNaN(v) && !Double.isInfinite(v)) out[n++] = v;
      }
    }
    return n == out.length ? out : Arrays.copyOf(out, n);
  }

  public static int[] snapshotInt(Deque<Integer> q) {
    Integer[] tmp;
    synchronized (q) {
      tmp = q.toArray(new Integer[0]);
    }
    int[] out = new int[tmp.length];
    int n = 0;
    for (Integer it : tmp) {
      if (it != null) out[n++] = it.intValue();
    }
    return n == out.length ? out : Arrays.copyOf(out, n);
  }
}
