package com.mealmesh.loyalty.entity;

public enum LoyaltyTier {
    BRONZE(1.0),
    SILVER(1.25),
    GOLD(1.5),
    PLATINUM(2.0);

    private final double multiplier;

    LoyaltyTier(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public static LoyaltyTier calculateTier(int lifetimePoints) {
        if (lifetimePoints >= 5000) return PLATINUM;
        if (lifetimePoints >= 2000) return GOLD;
        if (lifetimePoints >= 500) return SILVER;
        return BRONZE;
    }
}
