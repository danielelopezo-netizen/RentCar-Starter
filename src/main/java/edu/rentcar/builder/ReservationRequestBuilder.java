package edu.rentcar.builder;

import edu.rentcar.composite.ServiceComponent;
import edu.rentcar.model.Customer;
import edu.rentcar.model.RentalMode;
import edu.rentcar.model.ReservationRequest;
import edu.rentcar.model.Vehicle;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Builder pattern implementation (RF-001) for constructing and validating rental reservation requests
 * independently of the JavaFX graphical user interface.
 */
public final class ReservationRequestBuilder {
    private Customer customer;
    private Vehicle vehicle;
    private LocalDate startDate;
    private LocalDate endDate;
    private RentalMode rentalMode;
    private final List<ServiceComponent> selectedServices = new ArrayList<>();
    private String paymentToken = "tok_approved";

    public ReservationRequestBuilder withCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public ReservationRequestBuilder withVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        return this;
    }

    public ReservationRequestBuilder withStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public ReservationRequestBuilder withEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public ReservationRequestBuilder withDates(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        return this;
    }

    public ReservationRequestBuilder withRentalMode(RentalMode rentalMode) {
        this.rentalMode = rentalMode;
        return this;
    }

    public ReservationRequestBuilder addService(ServiceComponent serviceComponent) {
        if (serviceComponent == null) {
            throw new IllegalArgumentException("Selected service or package cannot be null.");
        }
        this.selectedServices.add(serviceComponent);
        return this;
    }

    public ReservationRequestBuilder withServices(List<? extends ServiceComponent> services) {
        this.selectedServices.clear();
        if (services != null) {
            for (ServiceComponent component : services) {
                addService(component);
            }
        }
        return this;
    }

    public ReservationRequestBuilder withPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
        return this;
    }

    /**
     * Validates all mandatory attributes and business rules before creating the ReservationRequest.
     *
     * @return a validated, immutable ReservationRequest
     * @throws IllegalArgumentException if any validation rule is violated
     */
    public ReservationRequest build() {
        validateCustomer();
        validateVehicle();
        validateDates();
        validateRentalMode();
        List<ServiceComponent> deduplicatedServices = validateAndDeduplicateServices();
        validatePaymentToken();

        return new ReservationRequest(
                customer,
                vehicle,
                startDate,
                endDate,
                rentalMode,
                deduplicatedServices,
                paymentToken.trim()
        );
    }

    private void validateCustomer() {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required.");
        }
        if (customer.getId() == null || customer.getId().isBlank()) {
            throw new IllegalArgumentException("Customer ID is invalid.");
        }
        if (customer.getFullName() == null || customer.getFullName().isBlank()) {
            throw new IllegalArgumentException("Customer full name is required.");
        }
    }

    private void validateVehicle() {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle is required.");
        }
        if (vehicle.getId() == null || vehicle.getId().isBlank()) {
            throw new IllegalArgumentException("Vehicle ID is invalid.");
        }
        if (!Double.isFinite(vehicle.getDailyRate()) || vehicle.getDailyRate() <= 0) {
            throw new IllegalArgumentException("Vehicle daily rate must be greater than zero.");
        }
    }

    private void validateDates() {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date is required.");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date is required.");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date must be strictly after start date (minimum 1 rental day).");
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days > 365) {
            throw new IllegalArgumentException("Rental duration cannot exceed 365 days.");
        }
    }

    private void validateRentalMode() {
        if (rentalMode == null) {
            throw new IllegalArgumentException("Rental modality is required.");
        }
    }

    private List<ServiceComponent> validateAndDeduplicateServices() {
        List<ServiceComponent> cleaned = new ArrayList<>();
        Set<String> seenComponentCodes = new LinkedHashSet<>();

        for (ServiceComponent component : selectedServices) {
            if (component == null) {
                throw new IllegalArgumentException("Selected service component cannot be null.");
            }
            if (seenComponentCodes.add(component.getCode())) {
                cleaned.add(component);
            }
        }
        return cleaned;
    }

    private void validatePaymentToken() {
        if (paymentToken == null || paymentToken.isBlank()) {
            throw new IllegalArgumentException("Payment token is required.");
        }
    }
}
