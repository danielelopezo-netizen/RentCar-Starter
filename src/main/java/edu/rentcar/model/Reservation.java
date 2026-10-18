package edu.rentcar.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain model representing a vehicle rental reservation and its lifecycle state (RF-002).
 * Enforces valid state transitions from PENDING to terminal states (CONFIRMED, REJECTED, FAILED).
 */
public class Reservation {
    private final String id;
    private final String customerId;
    private final String customerName;
    private final String vehicleId;
    private final String vehicleName;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final RentalMode rentalMode;
    private final BigDecimal totalCost;
    private final String description;
    private ReservationStatus reservationStatus;
    private String statusMessage;

    /**
     * Backward-compatible constructor matching the starter signature.
     */
    public Reservation(String id, String customerId, String vehicleId,
                       LocalDate startDate, LocalDate endDate, String status) {
        this(
                id,
                customerId,
                customerId,
                vehicleId,
                vehicleId,
                startDate,
                endDate,
                RentalMode.BASIC,
                BigDecimal.ZERO,
                "",
                parseStatus(status),
                ""
        );
    }

    public Reservation(String id,
                       ReservationRequest request,
                       BigDecimal totalCost,
                       String description,
                       ReservationStatus reservationStatus,
                       String statusMessage) {
        this(
                id,
                request.getCustomer().getId(),
                request.getCustomer().getFullName(),
                request.getVehicle().getId(),
                request.getVehicle().getDisplayName(),
                request.getStartDate(),
                request.getEndDate(),
                request.getRentalMode(),
                totalCost,
                description,
                reservationStatus,
                statusMessage
        );
    }

    public Reservation(String id,
                       String customerId,
                       String customerName,
                       String vehicleId,
                       String vehicleName,
                       LocalDate startDate,
                       LocalDate endDate,
                       RentalMode rentalMode,
                       BigDecimal totalCost,
                       String description,
                       ReservationStatus reservationStatus,
                       String statusMessage) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Reservation ID is required.");
        }
        this.id = id.trim();
        this.customerId = Objects.requireNonNull(customerId, "Customer ID is required.");
        this.customerName = customerName == null ? customerId : customerName;
        this.vehicleId = Objects.requireNonNull(vehicleId, "Vehicle ID is required.");
        this.vehicleName = vehicleName == null ? vehicleId : vehicleName;
        this.startDate = Objects.requireNonNull(startDate, "Start date is required.");
        this.endDate = Objects.requireNonNull(endDate, "End date is required.");
        this.rentalMode = Objects.requireNonNullElse(rentalMode, RentalMode.BASIC);
        this.totalCost = Objects.requireNonNullElse(totalCost, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        this.description = Objects.requireNonNullElse(description, "");
        this.reservationStatus = Objects.requireNonNull(reservationStatus, "Reservation status is required.");
        this.statusMessage = Objects.requireNonNullElse(statusMessage, "");
    }

    public synchronized void transitionTo(ReservationStatus targetStatus, String message) {
        Objects.requireNonNull(targetStatus, "Target reservation status is required.");
        if (this.reservationStatus == targetStatus) {
            this.statusMessage = Objects.requireNonNullElse(message, this.statusMessage);
            return;
        }
        if (this.reservationStatus != ReservationStatus.PENDING) {
            throw new IllegalStateException(
                    "Invalid reservation state transition from " + this.reservationStatus + " to " + targetStatus + "."
            );
        }
        this.reservationStatus = targetStatus;
        this.statusMessage = Objects.requireNonNullElse(message, "");
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public RentalMode getRentalMode() {
        return rentalMode;
    }

    public String getModalityName() {
        return rentalMode.getDisplayName();
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public String getDescription() {
        return description;
    }

    public synchronized ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public synchronized String getStatus() {
        return reservationStatus.name();
    }

    public synchronized String getStatusMessage() {
        return statusMessage;
    }

    public synchronized boolean isConfirmed() {
        return reservationStatus == ReservationStatus.CONFIRMED;
    }

    private static ReservationStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return ReservationStatus.PENDING;
        }
        try {
            return ReservationStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ReservationStatus.PENDING;
        }
    }
}
