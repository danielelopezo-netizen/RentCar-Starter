package edu.rentcar.decorator;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

/**
 * Base Decorator in the Decorator pattern (RF-005).
 * Wraps a BillableRental instance and delegates core operations to the wrapped component.
 */
public abstract class RentalDecorator implements BillableRental {
    protected final BillableRental wrappedRental;

    protected RentalDecorator(BillableRental wrappedRental) {
        this.wrappedRental = Objects.requireNonNull(wrappedRental, "Wrapped rental cannot be null.");
    }

    public BillableRental getWrappedRental() {
        return wrappedRental;
    }

    @Override
    public long getRentalDays() {
        return wrappedRental.getRentalDays();
    }

    @Override
    public String getDescription() {
        return wrappedRental.getDescription();
    }

    @Override
    public BigDecimal calculateCost() {
        return wrappedRental.calculateCost();
    }

    @Override
    public Set<String> getAppliedServiceCodes() {
        return wrappedRental.getAppliedServiceCodes();
    }
}
