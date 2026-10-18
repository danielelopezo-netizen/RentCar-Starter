package edu.rentcar.app;

import edu.rentcar.adapter.PaymentAdapter;
import edu.rentcar.adapter.PaymentGateway;
import edu.rentcar.facade.RentalFacade;
import edu.rentcar.proxy.VerifiedPaymentProxy;
import edu.rentcar.repository.CustomerRepository;
import edu.rentcar.repository.ReservationRepository;
import edu.rentcar.repository.VehicleRepository;
import edu.rentcar.service.AvailabilityService;
import edu.rentcar.service.CustomerVerificationService;
import edu.rentcar.service.PricingService;
import edu.rentcar.service.VehicleService;

/**
 * ApplicationServices es un Service Locator: los controllers consultan una clase global para obtener los servicios.
 */
public final class ApplicationServices {
    private static final CustomerRepository CUSTOMER_REPOSITORY = new CustomerRepository();
    private static final VehicleRepository VEHICLE_REPOSITORY = new VehicleRepository();
    private static final ReservationRepository RESERVATION_REPOSITORY = new ReservationRepository();

    private static final VehicleService VEHICLE_SERVICE = new VehicleService(VEHICLE_REPOSITORY);
    private static final AvailabilityService AVAILABILITY_SERVICE = new AvailabilityService(RESERVATION_REPOSITORY);
    private static final PricingService PRICING_SERVICE = new PricingService();
    private static final CustomerVerificationService VERIFICATION_SERVICE =
            new CustomerVerificationService(CUSTOMER_REPOSITORY);

    private static final PaymentGateway PAYMENT_GATEWAY = new VerifiedPaymentProxy(
            new PaymentAdapter(),
            VERIFICATION_SERVICE
    );

    private static final RentalFacade RENTAL_FACADE = new RentalFacade(
            RESERVATION_REPOSITORY,
            AVAILABILITY_SERVICE,
            PRICING_SERVICE,
            PAYMENT_GATEWAY
    );

    private ApplicationServices() {
    }

    public static VehicleService vehicleService() {
        return VEHICLE_SERVICE;
    }

    public static CustomerRepository customerRepository() {
        return CUSTOMER_REPOSITORY;
    }

    public static ReservationRepository reservationRepository() {
        return RESERVATION_REPOSITORY;
    }

    public static AvailabilityService availabilityService() {
        return AVAILABILITY_SERVICE;
    }

    public static PricingService pricingService() {
        return PRICING_SERVICE;
    }

    public static CustomerVerificationService verificationService() {
        return VERIFICATION_SERVICE;
    }

    public static RentalFacade rentalFacade() {
        return RENTAL_FACADE;
    }
}
