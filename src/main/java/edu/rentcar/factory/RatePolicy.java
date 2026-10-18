package edu.rentcar.factory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Abstract Product A in the Abstract Factory pattern (RF-003).
 * Defines the tariff calculation policy for a specific rental modality.
 */
public interface RatePolicy {

    String getPolicyName();

    String getConditionsDescription();

    /**
     * Calculates the base vehicle cost for the given daily rate and duration.
     */
    default BigDecimal calculateBaseVehicleCost(BigDecimal vehicleDailyRate, long rentalDays) {
        Objects.requireNonNull(vehicleDailyRate, "Vehicle daily rate is required.");
        if (rentalDays <= 0) {
            throw new IllegalArgumentException("Rental days must be positive.");
        }
        return vehicleDailyRate.multiply(BigDecimal.valueOf(rentalDays)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the additional modality surcharge or fee based on the vehicle daily rate and duration.
     */
    BigDecimal calculateModalityFee(BigDecimal vehicleDailyRate, long rentalDays);
}
