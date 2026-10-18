package edu.rentcar.builder;

import edu.rentcar.composite.ServiceCatalog;
import edu.rentcar.composite.ServiceComponent;
import edu.rentcar.model.Customer;
import edu.rentcar.model.RentalMode;
import edu.rentcar.model.ReservationRequest;
import edu.rentcar.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RF-001 (Builder Pattern: ReservationRequestBuilder).
 */
class ReservationRequestBuilderTest {

    private Customer validCustomer;
    private Vehicle validVehicle;
    private LocalDate validStart;
    private LocalDate validEnd;

    @BeforeEach
    void setUp() {
        validCustomer = new Customer("CUS-1001", "Sofia Martinez", "sofia@example.com", true);
        validVehicle = new Vehicle("VEH-1001", "Toyota", "Corolla", "Economy", 2024, 150000, "Available");
        validStart = LocalDate.of(2026, 10, 20);
        validEnd = LocalDate.of(2026, 10, 23);
    }

    @Test
    void buildValidReservationRequestShouldSucceed() {
        ReservationRequest request = new ReservationRequestBuilder()
                .withCustomer(validCustomer)
                .withVehicle(validVehicle)
                .withDates(validStart, validEnd)
                .withRentalMode(RentalMode.STANDARD)
                .addService(ServiceCatalog.gpsService())
                .addService(ServiceCatalog.childSeatService())
                .withPaymentToken("tok_approved")
                .build();

        assertNotNull(request);
        assertEquals(validCustomer, request.getCustomer());
        assertEquals(validVehicle, request.getVehicle());
        assertEquals(validStart, request.getStartDate());
        assertEquals(validEnd, request.getEndDate());
        assertEquals(3L, request.getRentalDays());
        assertEquals(RentalMode.STANDARD, request.getRentalMode());
        assertEquals(2, request.getSelectedServices().size());
        assertEquals("tok_approved", request.getPaymentToken());
    }

    @Test
    void buildWithDuplicateServiceComponentsShouldDeduplicateAutomatically() {
        ServiceComponent gps = ServiceCatalog.gpsService();
        ReservationRequest request = new ReservationRequestBuilder()
                .withCustomer(validCustomer)
                .withVehicle(validVehicle)
                .withDates(validStart, validEnd)
                .withRentalMode(RentalMode.BASIC)
                .addService(gps)
                .addService(gps)
                .build();

        assertEquals(1, request.getSelectedServices().size());
    }

    @Test
    void buildWithNullCustomerShouldThrowException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ReservationRequestBuilder()
                        .withVehicle(validVehicle)
                        .withDates(validStart, validEnd)
                        .withRentalMode(RentalMode.BASIC)
                        .build()
        );
        assertEquals("Customer is required.", ex.getMessage());
    }

    @Test
    void buildWithBlankCustomerIdShouldThrowException() {
        Customer invalidCustomer = new Customer("   ", "Sofia", "sofia@example.com", true);
        assertThrows(IllegalArgumentException.class, () ->
                new ReservationRequestBuilder()
                        .withCustomer(invalidCustomer)
                        .withVehicle(validVehicle)
                        .withDates(validStart, validEnd)
                        .withRentalMode(RentalMode.BASIC)
                        .build()
        );
    }

    @Test
    void buildWithNullVehicleShouldThrowException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ReservationRequestBuilder()
                        .withCustomer(validCustomer)
                        .withDates(validStart, validEnd)
                        .withRentalMode(RentalMode.BASIC)
                        .build()
        );
        assertEquals("Vehicle is required.", ex.getMessage());
    }

    @Test
    void buildWithInvalidVehicleRateShouldThrowException() {
        Vehicle zeroRateVehicle = new Vehicle("VEH-99", "Brand", "Model", "Economy", 2024, 0.0, "Available");
        assertThrows(IllegalArgumentException.class, () ->
                new ReservationRequestBuilder()
                        .withCustomer(validCustomer)
                        .withVehicle(zeroRateVehicle)
                        .withDates(validStart, validEnd)
                        .withRentalMode(RentalMode.BASIC)
                        .build()
        );
    }

    @Test
    void buildWithSameStartAndEndDateShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                new ReservationRequestBuilder()
                        .withCustomer(validCustomer)
                        .withVehicle(validVehicle)
                        .withDates(validStart, validStart)
                        .withRentalMode(RentalMode.BASIC)
                        .build()
        );
    }

    @Test
    void buildWithEndDateBeforeStartDateShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                new ReservationRequestBuilder()
                        .withCustomer(validCustomer)
                        .withVehicle(validVehicle)
                        .withDates(validEnd, validStart)
                        .withRentalMode(RentalMode.BASIC)
                        .build()
        );
    }

    @Test
    void buildWithNullRentalModeShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                new ReservationRequestBuilder()
                        .withCustomer(validCustomer)
                        .withVehicle(validVehicle)
                        .withDates(validStart, validEnd)
                        .withRentalMode(null)
                        .build()
        );
    }

    @Test
    void addNullServiceShouldThrowException() {
        ReservationRequestBuilder builder = new ReservationRequestBuilder();
        assertThrows(IllegalArgumentException.class, () -> builder.addService(null));
    }

    @Test
    void buildWithBlankPaymentTokenShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                new ReservationRequestBuilder()
                        .withCustomer(validCustomer)
                        .withVehicle(validVehicle)
                        .withDates(validStart, validEnd)
                        .withRentalMode(RentalMode.BASIC)
                        .withPaymentToken("   ")
                        .build()
        );
    }
}
