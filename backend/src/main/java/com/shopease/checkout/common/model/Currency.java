package com.shopease.checkout.common.model;

public enum Currency {
    USD(1.0),
    GHS(15.5),   // 1 USD = 15.5 GHS
    EUR(0.92);   // 1 USD = 0.92 EUR

    private final double rateToUsd;

    Currency(double rateToUsd) {
        this.rateToUsd = rateToUsd;
    }

    /** How many units of this currency equal 1 USD */
    public double getRateToUsd() {
        return rateToUsd;
    }
}
