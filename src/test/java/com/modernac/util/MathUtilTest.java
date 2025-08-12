package com.modernac.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

public class MathUtilTest {
    @Test
    public void testFindGcd() {
        double gcd = MathUtil.findGcd(Arrays.asList(1.5, 3.0, 4.5));
        assertTrue(Math.abs(gcd - 1.5) < 1e-3);
    }

    @Test
    public void testIqr() {
        double iqr = MathUtil.iqr(Arrays.asList(1.0, 2.0, 3.0, 4.0));
        assertEquals(2.0, iqr, 1e-9);
    }
}
