package edu.rentcar.factory;

import edu.rentcar.composite.ServiceCatalog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * Concrete CoveragePolicy for the PREMIUM rental modality (45,000 COP/day, includes GPS and Additional Driver).
 */
public final class PremiumCoveragePolicy implements CoveragePolicy {
    private static final BigDecimal DAILY_FEE = BigDecimal.valueOf(45000).setScale(2, RoundingMode.HALF_UP);

    @Override
    public String getCoverageName() {
        return "Full All-Risk Zero-Deductible Coverage";
    }

    @Override
    public String getCoverageDescription() {
        return "100% full coverage with zero deductible (collision, theft, glass & tires) + complimentary GPS & Additional Driver.";
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
        return Set.of(ServiceCatalog.CODE_GPS, ServiceCatalog.CODE_ADDITIONAL_DRIVER);
    }
}
