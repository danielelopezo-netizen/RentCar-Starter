package edu.rentcar.composite;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Component abstraction in the Composite pattern (RF-007).
 * Allows uniform treatment of single optional services and nested service packages.
 */
public interface ServiceComponent {

    String getCode();

    String getName();

    String getDescription();

    boolean isDailyRate();

    /**
     * Calculates the total cost of this service or package for the specified rental duration.
     *
     * @param rentalDays number of rental days (must be positive)
     * @return monetary cost rounded to two decimal places
     */
    BigDecimal calculateCost(long rentalDays);

    /**
     * Returns all atomic leaf service codes contained within this component (for deduplication).
     */
    Set<String> getIncludedServiceCodes();
}
