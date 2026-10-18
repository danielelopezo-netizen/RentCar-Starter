package edu.rentcar.factory;

import edu.rentcar.composite.ServiceCatalog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * Concrete CoveragePolicy for the STANDARD rental modality (25,000 COP/day, includes GPS Navigation).
 */
public final class StandardCoveragePolicy implements CoveragePolicy {
    private static final BigDecimal DAILY_FEE = BigDecimal.valueOf(25000).setScale(2, RoundingMode.HALF_UP);

    @Override
    public String getCoverageName() {
        return "Partial Collision & Theft Protection (CDW)";
    }

    @Override
    public String getCoverageDescription() {
        return "50% reduced deductible for collision/theft + complimentary GPS Navigation included.";
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
        return DAILY_FEE.multiply(BigDecimal.valueOf(rentalDays)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Set<String> getIncludedServiceCodes() {
        return Set.of(ServiceCatalog.CODE_GPS);
    }
}
