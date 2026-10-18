package edu.rentcar.decorator;

import edu.rentcar.composite.ServiceCatalog;

/**
 * Concrete Decorator in the Decorator pattern for adding GPS Navigation (RF-005).
 */
public final class GpsServiceDecorator extends AdditionalServiceDecorator {

    public GpsServiceDecorator(BillableRental wrappedRental) {
        super(wrappedRental, ServiceCatalog.gpsService());
    }
}
