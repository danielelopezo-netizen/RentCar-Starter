package edu.rentcar.factory;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Abstract Product B in the Abstract Factory pattern (RF-003).
 * Defines the insurance coverage, deductible, and complimentary included services for a rental modality.
 */
public interface CoveragePolicy {

    String getCoverageName();

    String getCoverageDescription();

    BigDecimal getDailyCoverageFee();

    BigDecimal calculateCoverageCost(long rentalDays);

    /**
     * Returns service codes already included at no extra cost in this modality.
     */
    Set<String> getIncludedServiceCodes();
}
