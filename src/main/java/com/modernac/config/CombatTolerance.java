package com.modernac.config;

public enum CombatTolerance {
    LENIENT(1.5),
    LOW(1.2),
    NORMAL(1.0),
    HIGH(0.8),
    AGGRESSIVE(0.6);

    private final double multiplier;

    CombatTolerance(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
