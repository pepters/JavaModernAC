package com.modernac.model;

/**
 * Simple rolling statistics using Welford's algorithm.
 */
public class RollingStats {
    private long n;
    private double mean;
    private double m2;

    public void add(double x) {
        n++;
        double delta = x - mean;
        mean += delta / n;
        double delta2 = x - mean;
        m2 += delta * delta2;
    }

    public long getCount() {
        return n;
    }

    public double getMean() {
        return mean;
    }

    public double getVariance() {
        return n > 1 ? m2 / (n - 1) : 0.0;
    }

    public double getStdDev() {
        return Math.sqrt(getVariance());
    }

    public double zScore(double x) {
        double sd = getStdDev();
        if (sd < 1e-9) return 0.0;
        return (x - mean) / sd;
    }
}
