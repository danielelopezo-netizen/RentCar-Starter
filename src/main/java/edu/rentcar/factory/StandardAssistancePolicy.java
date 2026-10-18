package edu.rentcar.factory;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Concrete AssistancePolicy for the STANDARD rental modality (10,000 COP/day).
 */
public final class StandardAssistancePolicy implements AssistancePolicy {
    private static final BigDecimal DAILY_FEE = BigDecimal.valueOf(10000).setScale(2, RoundingMode.HALF_UP);

    @Override
    public String getAssistanceName() {
        return "24/7 Basic Roadside Assistance";
    }

    @Override
    public String getAssistanceDescription() {
        return "24/7 emergency phone support, battery jump-start, tire change, and towing up to 50 km.";
    }

    @Override
    public BigDecimal getDailyAssistanceFee() {
        return DAILY_FEE;
    }

    @Override
    public BigDecimal calculateAssistanceCost(long rentalDays) {
        if (rentalDays <= 0) {
            throw new IllegalArgumentException("Rental days must be positive.");
        }
        return DAILY_FEE.multiply(BigDecimal.valueOf(rentalDays)).setScale(2, RoundingMode.HALF_UP);
    }
}
