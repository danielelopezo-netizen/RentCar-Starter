package edu.rentcar.factory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Concrete RatePolicy for the PREMIUM rental modality (+20% modality surcharge on base vehicle rate).
 */
public final class PremiumRatePolicy implements RatePolicy {
    private static final BigDecimal SURCHARGE_RATE = new BigDecimal("0.20");

    @Override
    public String getPolicyName() {
        return "Premium Unlimited Tariff Policy";
    }

    @Override
    public String getConditionsDescription() {
        return "Base vehicle rate + 20% VIP modality surcharge, unlimited mileage, priority vehicle delivery and flexible return.";
    }

    @Override
    public BigDecimal calculateModalityFee(BigDecimal vehicleDailyRate, long rentalDays) {
        Objects.requireNonNull(vehicleDailyRate, "Vehicle daily rate is required.");
        if (rentalDays <= 0) {
            throw new IllegalArgumentException("Rental days must be positive.");
        }
        return calculateBaseVehicleCost(vehicleDailyRate, rentalDays)
                .multiply(SURCHARGE_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
