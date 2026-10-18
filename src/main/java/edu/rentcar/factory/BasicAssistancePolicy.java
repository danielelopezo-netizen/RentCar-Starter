package edu.rentcar.factory;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Concrete AssistancePolicy for the BASIC rental modality.
 */
public final class BasicAssistancePolicy implements AssistancePolicy {
    private static final BigDecimal DAILY_FEE = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Override
    public String getAssistanceName() {
        return "Standard Business-Hours Support";
    }

    @Override
    public String getAssistanceDescription() {
        return "Telephone customer support during regular business hours (8:00 AM - 6:00 PM).";
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
        return DAILY_FEE;
    }
}
