package edu.rentcar.decorator;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Component interface in the Decorator pattern (RF-005).
 * Represents a billable rental item whose cost and description can be dynamically extended.
 */
public interface BillableRental {

    long getRentalDays();

    String getDescription();

    BigDecimal calculateCost();

    /**
     * Returns all service codes already applied or included in this billable rental,
     * preventing accidental double-charging of duplicate services.
     */
    Set<String> getAppliedServiceCodes();
}
