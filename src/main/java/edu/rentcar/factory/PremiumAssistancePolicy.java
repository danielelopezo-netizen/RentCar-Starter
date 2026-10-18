package edu.rentcar.factory;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Concrete AssistancePolicy for the PREMIUM rental modality (20,000 COP/day).
 */
public final class PremiumAssistancePolicy implements AssistancePolicy {
    private static final BigDecimal DAILY_FEE = BigDecimal.valueOf(20000).setScale(2, RoundingMode.HALF_UP);

    @Override
    public String getAssistanceName() {
        return "24/7 VIP Roadside & Replacement Vehicle Assistance";
    }

    @Override
    public String getAssistanceDescription() {
        return "24/7 nationwide towing, immediate replacement vehicle, medical & travel coverage, and concierge service.";
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
