package edu.rentcar.facade;

import com.rentcar.paymentclient.PaymentApiResponse;
import edu.rentcar.adapter.PaymentAdapter;
import edu.rentcar.adapter.PaymentGateway;
import edu.rentcar.builder.ReservationRequestBuilder;
import edu.rentcar.composite.ServiceCatalog;
import edu.rentcar.model.Customer;
import edu.rentcar.model.RentalMode;
import edu.rentcar.model.RentalOperationResult;
import edu.rentcar.model.ReservationRequest;
import edu.rentcar.model.ReservationStatus;
import edu.rentcar.model.Vehicle;
import edu.rentcar.proxy.VerifiedPaymentProxy;
import edu.rentcar.repository.ReservationRepository;
import edu.rentcar.service.AvailabilityService;
import edu.rentcar.service.CustomerVerificationService;
import edu.rentcar.service.PricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RF-002 (Facade Pattern: RentalFacade, AvailabilityService, and full rental lifecycle).
 */
class RentalFacadeTest {

    private ReservationRepository reservationRepository;
    private AvailabilityService availabilityService;
    private PricingService pricingService;
    private Customer authorizedCustomer;
    private Customer unauthorizedCustomer;
    private Vehicle vehicle;
    private LocalDate start;
    private LocalDate end;

    @BeforeEach
    void setUp() {
        reservationRepository = new ReservationRepository();
        availabilityService = new AvailabilityService(reservationRepository);
        pricingService = new PricingService();

        authorizedCustomer = new Customer("CUS-1001", "Sofia Martinez", "sofia@example.com", true);
        unauthorizedCustomer = new Customer("CUS-1003", "Valentina Torres", "valentina@example.com", false);
        vehicle = new Vehicle("VEH-1001", "Toyota", "Corolla", "Economy", 2024, 150000, "Available");
        start = LocalDate.of(2026, 10, 20);
        end = LocalDate.of(2026, 10, 23); // 3 days
    }

    private RentalFacade createFacadeWithMockApi(PaymentAdapter.ExternalPaymentCaller caller) {
        PaymentGateway adapter = new PaymentAdapter(caller);
        PaymentGateway proxy = new VerifiedPaymentProxy(adapter, new CustomerVerificationService());
        return new RentalFacade(reservationRepository, availabilityService, pricingService, proxy);
    }

    @Test
    void successfulRentalShouldConfirmReservationAndAffectSubsequentAvailability() {
        RentalFacade facade = createFacadeWithMockApi(req -> new PaymentApiResponse(200, "APPROVED"));

        ReservationRequest request = new ReservationRequestBuilder()
                .withCustomer(authorizedCustomer)
                .withVehicle(vehicle)
                .withDates(start, end)
                .withRentalMode(RentalMode.STANDARD)
                .addService(ServiceCatalog.childSeatService())
                .build();

        assertTrue(facade.isVehicleAvailable(vehicle.getId(), start, end));

        RentalOperationResult result = facade.processRental(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getReservation());
        assertEquals(ReservationStatus.CONFIRMED, result.getReservation().getReservationStatus());
        // Base 450,000 + Modality 45,000 + Coverage 75,000 + Assistance 30,000 + Child Seat 60,000 = 660,000.00
        assertEquals(new BigDecimal("660000.00"), result.getReservation().getTotalCost());

        // Subsequent availability check for same or overlapping dates must return false
        assertFalse(facade.isVehicleAvailable(vehicle.getId(), start, end));
        assertFalse(facade.isVehicleAvailable(vehicle.getId(), start.plusDays(1), end.plusDays(1)));
        // Consecutive rental starting on the return date must be allowed
        assertTrue(facade.isVehicleAvailable(vehicle.getId(), end, end.plusDays(2)));
    }

    @Test
    void overlappingRentalRequestForUnavailableVehicleShouldBeRejectedWithoutInvokingPayment() {
        AtomicInteger paymentCalls = new AtomicInteger(0);
        RentalFacade facade = createFacadeWithMockApi(req -> {
            paymentCalls.incrementAndGet();
            return new PaymentApiResponse(200, "APPROVED");
        });

        ReservationRequest firstRequest = new ReservationRequestBuilder()
                .withCustomer(authorizedCustomer)
                .withVehicle(vehicle)
                .withDates(start, end)
                .withRentalMode(RentalMode.BASIC)
                .build();

        RentalOperationResult firstResult = facade.processRental(firstRequest);
        assertTrue(firstResult.isSuccess());
        assertEquals(1, paymentCalls.get());

        // Overlapping second request [2026-10-21 to 2026-10-25]
        ReservationRequest overlappingRequest = new ReservationRequestBuilder()
                .withCustomer(authorizedCustomer)
                .withVehicle(vehicle)
                .withDates(start.plusDays(1), end.plusDays(2))
                .withRentalMode(RentalMode.BASIC)
                .build();

        RentalOperationResult secondResult = facade.processRental(overlappingRequest);

        assertFalse(secondResult.isSuccess());
        assertEquals(ReservationStatus.REJECTED, secondResult.getReservation().getReservationStatus());
        assertEquals(1, paymentCalls.get(), "Payment must not be called when vehicle is unavailable.");
    }

    @Test
    void declinedPaymentShouldRejectReservationAndKeepVehicleAvailable() {
        RentalFacade facade = createFacadeWithMockApi(req -> new PaymentApiResponse(200, "NOT_APPROVED"));

        ReservationRequest request = new ReservationRequestBuilder()
                .withCustomer(authorizedCustomer)
                .withVehicle(vehicle)
                .withDates(start, end)
                .withRentalMode(RentalMode.BASIC)
                .withPaymentToken("tok_declined")
                .build();

        RentalOperationResult result = facade.processRental(request);

        assertFalse(result.isSuccess());
        assertEquals(ReservationStatus.REJECTED, result.getReservation().getReservationStatus());
        assertEquals(0L, facade.getConfirmedReservationCount());
        // Vehicle must remain available because payment was declined
        assertTrue(facade.isVehicleAvailable(vehicle.getId(), start, end));
    }

    @Test
    void unauthorizedCustomerShouldRejectReservationWithoutCallingExternalApiAndKeepVehicleAvailable() {
        AtomicInteger externalApiCalls = new AtomicInteger(0);
        RentalFacade facade = createFacadeWithMockApi(req -> {
            externalApiCalls.incrementAndGet();
            return new PaymentApiResponse(200, "APPROVED");
        });

        ReservationRequest request = new ReservationRequestBuilder()
                .withCustomer(unauthorizedCustomer)
                .withVehicle(vehicle)
                .withDates(start, end)
                .withRentalMode(RentalMode.BASIC)
                .build();

        RentalOperationResult result = facade.processRental(request);

        assertFalse(result.isSuccess());
        assertEquals(ReservationStatus.REJECTED, result.getReservation().getReservationStatus());
        assertEquals(0, externalApiCalls.get(), "External payment API must not be called for unauthorized customer.");
        assertTrue(facade.isVehicleAvailable(vehicle.getId(), start, end));
    }

    @Test
    void paymentConnectionFailureShouldMarkReservationAsFailedAndKeepVehicleAvailable() {
        RentalFacade facade = createFacadeWithMockApi(req -> {
            throw new IOException("Connection timed out");
        });

        ReservationRequest request = new ReservationRequestBuilder()
                .withCustomer(authorizedCustomer)
                .withVehicle(vehicle)
                .withDates(start, end)
                .withRentalMode(RentalMode.BASIC)
                .build();

        RentalOperationResult result = facade.processRental(request);

        assertFalse(result.isSuccess());
        assertEquals(ReservationStatus.FAILED, result.getReservation().getReservationStatus());
        assertTrue(facade.isVehicleAvailable(vehicle.getId(), start, end));
    }
}
