package edu.rentcar.factory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * Concrete CoveragePolicy for the BASIC rental modality.
 */
public final class BasicCoveragePolicy implements CoveragePolicy {
    private static final BigDecimal DAILY_FEE = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Override
    public String getCoverageName() {
        return "Mandatory Third-Party Liability Coverage";
    }

    @Override
    public String getCoverageDescription() {
        return "Includes mandatory civil liability insurance with standard deductible. No extra services included.";
    }

    @Override
    public BigDecimal getDailyCoverageFee() {
        return DAILY_FEE;
    }

    @Override
    public BigDecimal calculateCoverageCost(long rentalDays) {
        if (rentalDays <= 0) {
            throw new IllegalArgumentException("Rental days must be positive.");
        }
        return DAILY_FEE;
    }

    @Override
    public Set<String> getIncludedServiceCodes() {
        return Set.of();
    }
}
