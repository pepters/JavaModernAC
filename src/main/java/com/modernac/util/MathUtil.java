package com.modernac.util;

import java.util.Arrays;

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
}
