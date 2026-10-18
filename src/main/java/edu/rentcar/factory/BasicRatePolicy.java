package edu.rentcar.factory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Concrete RatePolicy for the BASIC rental modality (0% modality surcharge).
 */
public final class BasicRatePolicy implements RatePolicy {

    @Override
    public String getPolicyName() {
        return "Basic Tariff Policy";
    }

    @Override
    public String getConditionsDescription() {
        return "Base vehicle daily rate (100%), standard 250 km/day mileage allowance, no modality surcharge.";
    }

    @Override
    public BigDecimal calculateModalityFee(BigDecimal vehicleDailyRate, long rentalDays) {
        Objects.requireNonNull(vehicleDailyRate, "Vehicle daily rate is required.");
        if (rentalDays <= 0) {
            throw new IllegalArgumentException("Rental days must be positive.");
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
