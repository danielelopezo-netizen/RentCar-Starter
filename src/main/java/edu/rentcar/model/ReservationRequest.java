package edu.rentcar.model;

import edu.rentcar.composite.ServiceComponent;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Validated, immutable rental reservation request built via ReservationRequestBuilder (RF-001).
 */
public final class ReservationRequest {
    private final Customer customer;
    private final Vehicle vehicle;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final RentalMode rentalMode;
    private final List<ServiceComponent> selectedServices;
    private final String paymentToken;

    public ReservationRequest(Customer customer,
                              Vehicle vehicle,
                              LocalDate startDate,
                              LocalDate endDate,
                              RentalMode rentalMode,
                              List<ServiceComponent> selectedServices,
                              String paymentToken) {
        this.customer = Objects.requireNonNull(customer, "Customer is required.");
        this.vehicle = Objects.requireNonNull(vehicle, "Vehicle is required.");
        this.startDate = Objects.requireNonNull(startDate, "Start date is required.");
        this.endDate = Objects.requireNonNull(endDate, "End date is required.");
        this.rentalMode = Objects.requireNonNull(rentalMode, "Rental mode is required.");
        this.selectedServices = selectedServices == null
                ? List.of()
                : Collections.unmodifiableList(selectedServices);
        this.paymentToken = (paymentToken == null || paymentToken.isBlank())
                ? "tok_approved"
                : paymentToken.trim();
    }

    public Customer getCustomer() {
        return customer;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public long getRentalDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public RentalMode getRentalMode() {
        return rentalMode;
    }

    public List<ServiceComponent> getSelectedServices() {
        return selectedServices;
    }

    public String getPaymentToken() {
        return paymentToken;
    }
}
