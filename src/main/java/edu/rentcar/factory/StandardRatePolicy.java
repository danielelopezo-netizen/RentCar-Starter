package edu.rentcar.factory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Concrete RatePolicy for the STANDARD rental modality (+10% modality surcharge on base vehicle rate).
 */
public final class StandardRatePolicy implements RatePolicy {
    private static final BigDecimal SURCHARGE_RATE = new BigDecimal("0.10");

    @Override
    public String getPolicyName() {
        return "Standard Flexible Tariff Policy";
    }

    @Override
    public String getConditionsDescription() {
        return "Base vehicle rate + 10% modality surcharge, extended 500 km/day mileage, free date modification.";
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
