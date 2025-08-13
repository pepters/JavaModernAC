package com.modernac.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class MathUtil {
  private MathUtil() {}

  /** Approximate gcd for a collection of doubles. */
  public static double findGcd(Collection<Double> values) {
    if (values.isEmpty()) return 0.0;
    double gcd = 0.0;
    for (double v : values) {
      if (gcd == 0.0) {
        gcd = v;
      } else {
        gcd = gcd(gcd, v);
      }
    }
    return gcd;
  }

  private static double gcd(double a, double b) {
    double tol = 1e-4;
    while (b > tol) {
      double t = b;
      b = a % b;
      a = t;
    }
    return a;
  }

  /** Compute interquartile range of collection. */
  public static double iqr(Collection<Double> values) {
    if (values.isEmpty()) return 0.0;
    List<Double> list = new ArrayList<>(values);
    Collections.sort(list);
    int n = list.size();
    double q1 = list.get(n / 4);
    double q3 = list.get(3 * n / 4);
    return q3 - q1;
  }
}
