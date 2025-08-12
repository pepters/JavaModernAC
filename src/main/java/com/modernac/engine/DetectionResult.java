package com.modernac.engine;

/**
 * Output of a check after evaluation.
 */
public class DetectionResult {
    private final String family;
    private final double evidenceScore; // 0..1
    private final Window window;
    private final boolean stabilityOK;
    private final boolean latencyOK;
    private final boolean normalized;

    public DetectionResult(String family, double evidenceScore, Window window,
                           boolean stabilityOK, boolean latencyOK, boolean normalized) {
        this.family = family;
        this.evidenceScore = evidenceScore;
        this.window = window;
        this.stabilityOK = stabilityOK;
        this.latencyOK = latencyOK;
        this.normalized = normalized;
    }

    public String getFamily() { return family; }
    public double getEvidenceScore() { return evidenceScore; }
    public Window getWindow() { return window; }
    public boolean isStabilityOK() { return stabilityOK; }
    public boolean isLatencyOK() { return latencyOK; }
    public boolean isNormalized() { return normalized; }
}
