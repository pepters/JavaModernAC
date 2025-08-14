package com.modernac.net;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks network conditions and provides per-player lag compensation
 * parameters.
 */
public final class LagCompensator {
  public static final class LagContext {
    public final int rttMs;
    public final double jitterMs;
    public final double tps;
    public final int oneWayMs;
    public final int dtRotAimMs;
    public final double yawRelax;
    public final boolean soft;

    LagContext(int rttMs, double jitterMs, double tps, int dtRotAimMs, double yawRelax, boolean soft) {
      this.rttMs = rttMs;
      this.jitterMs = jitterMs;
      this.tps = tps;
      this.oneWayMs = Math.max(0, rttMs / 2);
      this.dtRotAimMs = dtRotAimMs;
      this.yawRelax = yawRelax;
      this.soft = soft;
    }
  }

  private static final class State {
    double rttEwma = -1;
    double jitterEwma = 0;
    double tpsEwma = 20;
    int lastRtt = -1;
  }

  private final ConcurrentHashMap<UUID, State> map = new ConcurrentHashMap<>();
  private final boolean enabled;
  private final double alpha;
  private final int dtBase;
  private final int dtMin;
  private final int dtMax;
  private final double yawRelaxPerJitter;
  private final int unstableLimitMs;
  private final double tpsSoftGuard;

  public LagCompensator(
      boolean enabled,
      double alpha,
      int dtBase,
      int dtMin,
      int dtMax,
      double yawRelaxPerJitter,
      int unstableLimitMs,
      double tpsSoftGuard) {
    this.enabled = enabled;
    this.alpha = alpha;
    this.dtBase = dtBase;
    this.dtMin = dtMin;
    this.dtMax = dtMax;
    this.yawRelaxPerJitter = yawRelaxPerJitter;
    this.unstableLimitMs = unstableLimitMs;
    this.tpsSoftGuard = tpsSoftGuard;
  }

  public void sample(UUID id, int rttMs, double tps) {
    if (!enabled) {
      return;
    }
    State st = map.computeIfAbsent(id, k -> new State());
    if (rttMs > 0) {
      if (st.rttEwma < 0) st.rttEwma = rttMs;
      double prev = st.lastRtt < 0 ? rttMs : st.lastRtt;
      double jitter = Math.abs(rttMs - prev);
      st.jitterEwma = (1 - alpha) * st.jitterEwma + alpha * jitter;
      st.rttEwma = (1 - alpha) * st.rttEwma + alpha * rttMs;
      st.lastRtt = rttMs;
    }
    st.tpsEwma = (1 - alpha) * st.tpsEwma + alpha * tps;
  }

  public LagContext estimate(UUID id) {
    if (!enabled) {
      return new LagContext(0, 0.0, 20.0, dtBase, 0.0, false);
    }
    State st = map.computeIfAbsent(id, k -> new State());
    int rtt = (int) Math.round(st.rttEwma < 0 ? 0 : st.rttEwma);
    double jitter = st.jitterEwma;
    double tps = st.tpsEwma;
    int dt = clamp((int) Math.round(dtBase + jitter * 0.5 + rtt * 0.25), dtMin, dtMax);
    double yawRelax = jitter * yawRelaxPerJitter;
    boolean soft = (rtt >= unstableLimitMs) || (tps < tpsSoftGuard);
    return new LagContext(rtt, jitter, tps, dt, yawRelax, soft);
  }

  private static int clamp(int v, int lo, int hi) {
    return Math.max(lo, Math.min(hi, v));
  }
}

