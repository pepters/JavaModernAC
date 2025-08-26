package com.modernac.net;

import static org.junit.Assert.*;

import java.util.UUID;
import org.junit.Test;

public class LagCompensatorTest {
  @Test
  public void testSmoothingAndStability() {
    LagCompensator comp =
        new LagCompensator(
            true,
            0.2,
            0.2,
            35,
            25,
            90,
            0.0,
            1000,
            18.0,
            160,
            25.0,
            19.0,
            75);
    UUID id = UUID.randomUUID();
    for (int i = 0; i < 5; i++) {
      comp.sample(id, 100, 20.0);
    }
    LagCompensator.LagContext ctx = comp.estimate(id);
    assertTrue(ctx.stable);
    comp.sample(id, 400, 20.0);
    LagCompensator.LagContext ctx2 = comp.estimate(id);
    assertFalse(ctx2.stable);
    assertTrue(ctx2.rttMs > ctx.rttMs);
  }
}
