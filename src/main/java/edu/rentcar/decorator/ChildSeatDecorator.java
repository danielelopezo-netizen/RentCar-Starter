package edu.rentcar.decorator;

import edu.rentcar.composite.ServiceCatalog;

/**
 * Concrete Decorator in the Decorator pattern for adding a Child Safety Seat (RF-005).
 */
public final class ChildSeatDecorator extends AdditionalServiceDecorator {

    public ChildSeatDecorator(BillableRental wrappedRental) {
        super(wrappedRental, ServiceCatalog.childSeatService());
    }
}
