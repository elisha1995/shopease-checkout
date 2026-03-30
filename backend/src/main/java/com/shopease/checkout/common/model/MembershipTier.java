package com.shopease.checkout.common.model;

public enum MembershipTier {
    STANDARD(0),
    SILVER(10),
    GOLD(20),
    PLATINUM(100); // 100% = free shipping

    private final int discountPercent;

    MembershipTier(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }
}
