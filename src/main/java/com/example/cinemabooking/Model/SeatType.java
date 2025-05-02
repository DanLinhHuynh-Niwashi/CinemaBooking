package com.example.cinemabooking.Model;

public enum SeatType {
    STANDARD(1.0),
    VIP(1.5),
    COUPLE(2.0);

    private final double priceMultiplier;

    SeatType(double priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }
}
