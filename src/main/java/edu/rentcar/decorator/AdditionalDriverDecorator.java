package edu.rentcar.decorator;

import edu.rentcar.composite.ServiceCatalog;

/**
 * Concrete Decorator in the Decorator pattern for adding an Additional Driver (RF-005).
 */
public final class AdditionalDriverDecorator extends AdditionalServiceDecorator {

    public AdditionalDriverDecorator(BillableRental wrappedRental) {
        super(wrappedRental, ServiceCatalog.additionalDriverService());
    }
}
