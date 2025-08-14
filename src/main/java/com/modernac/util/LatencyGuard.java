package com.modernac.util;

public final class LatencyGuard {
  private LatencyGuard() {}

  public static boolean isStable(int pingMs, double tps, int pingLimitMs, double tpsSoft) {
    return pingMs <= pingLimitMs && tps >= tpsSoft;
  }

  public static boolean shouldSkip(int pingMs, double tps, int pingLimitMs, double tpsSoft) {
    return !isStable(pingMs, tps, pingLimitMs, tpsSoft);
  }
}
