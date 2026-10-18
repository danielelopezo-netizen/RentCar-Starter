package edu.rentcar.facade;

import edu.rentcar.adapter.PaymentGateway;
import edu.rentcar.model.PaymentRequest;
import edu.rentcar.model.PaymentResult;
import edu.rentcar.model.PaymentStatus;
import edu.rentcar.model.PricingBreakdown;
import edu.rentcar.model.RentalOperationResult;
import edu.rentcar.model.Reservation;
import edu.rentcar.model.ReservationRequest;
import edu.rentcar.model.ReservationStatus;
import edu.rentcar.repository.ReservationRepository;
import edu.rentcar.service.AvailabilityService;
import edu.rentcar.service.PricingService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Facade pattern implementation (RF-002).
 * Central coordinator for the complete vehicle rental workflow:
 * 1. Validates request and checks vehicle availability.
 * 2. Calculates pricing breakdown (via Abstract Factory, Decorator, and Composite).
 * 3. Registers initial PENDING reservation in memory.
 * 4. Invokes payment processing through the PaymentGateway (Protected by Proxy -> Adapter -> External API).
 * 5. Updates reservation status to CONFIRMED, REJECTED, or FAILED atomically.
 */
public class RentalFacade {
    private final ReservationRepository reservationRepository;
    private final AvailabilityService availabilityService;
    private final PricingService pricingService;
    private final PaymentGateway paymentGateway;
    private final Object reservationLock = new Object();

    public RentalFacade(ReservationRepository reservationRepository,
                        AvailabilityService availabilityService,
                        PricingService pricingService,
                        PaymentGateway paymentGateway) {
        this.reservationRepository = Objects.requireNonNull(reservationRepository, "ReservationRepository is required.");
        this.availabilityService = Objects.requireNonNull(availabilityService, "AvailabilityService is required.");
        this.pricingService = Objects.requireNonNull(pricingService, "PricingService is required.");
        this.paymentGateway = Objects.requireNonNull(paymentGateway, "PaymentGateway is required.");
    }

    /**
     * Previews the pricing breakdown for a validated reservation request without registering or charging.
     */
    public PricingBreakdown previewPricing(ReservationRequest request) {
        Objects.requireNonNull(request, "ReservationRequest cannot be null.");
        return pricingService.calculateBreakdown(request);
    }

    /**
     * Checks whether a vehicle is available for the requested date range.
     */
    public boolean isVehicleAvailable(String vehicleId, LocalDate startDate, LocalDate endDate) {
        return availabilityService.isVehicleAvailable(vehicleId, startDate, endDate);
    }

    /**
     * Executes the complete rental reservation and payment workflow in a thread-safe manner.
     */
    public RentalOperationResult processRental(ReservationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Reservation request cannot be null.");
        }

        synchronized (reservationLock) {
            PricingBreakdown breakdown = pricingService.calculateBreakdown(request);
            String reservationId = reservationRepository.nextReservationId();

            Reservation reservation = new Reservation(
                    reservationId,
                    request,
                    breakdown.getTotalCost(),
                    breakdown.getFullDescription(),
                    ReservationStatus.PENDING,
                    "Pending availability and payment processing."
            );

            boolean available = availabilityService.isVehicleAvailable(
                    request.getVehicle().getId(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            if (!available) {
                String msg = "Vehicle " + request.getVehicle().getDisplayName()
                        + " (" + request.getVehicle().getId() + ") is not available for the selected dates ("
                        + request.getStartDate() + " to " + request.getEndDate() + ").";
                reservation.transitionTo(ReservationStatus.REJECTED, msg);
                reservationRepository.save(reservation);
                return new RentalOperationResult(false, reservation, breakdown, null, msg);
            }

            // Save initial PENDING state before invoking payment
            reservationRepository.save(reservation);

            PaymentRequest paymentRequest = new PaymentRequest(
                    reservationId,
                    request.getCustomer(),
                    breakdown.getTotalCost(),
                    request.getPaymentToken()
            );

            PaymentResult paymentResult = paymentGateway.processPayment(paymentRequest);

            if (paymentResult.isApproved()) {
                String successMsg = "Reservation " + reservationId + " confirmed! Payment approved for "
                        + breakdown.getTotalCost() + " COP.";
                reservation.transitionTo(ReservationStatus.CONFIRMED, successMsg);
                reservationRepository.save(reservation);
                return new RentalOperationResult(true, reservation, breakdown, paymentResult, successMsg);
            }

            if (paymentResult.getStatus() == PaymentStatus.DECLINED
                    || paymentResult.getStatus() == PaymentStatus.UNAUTHORIZED_CUSTOMER) {
                String rejectedMsg = "Reservation " + reservationId + " rejected: " + paymentResult.getMessage();
                reservation.transitionTo(ReservationStatus.REJECTED, rejectedMsg);
                reservationRepository.save(reservation);
                return new RentalOperationResult(false, reservation, breakdown, paymentResult, rejectedMsg);
            }

            String failedMsg = "Reservation " + reservationId + " failed due to payment service error: "
                    + paymentResult.getMessage();
            reservation.transitionTo(ReservationStatus.FAILED, failedMsg);
            reservationRepository.save(reservation);
            return new RentalOperationResult(false, reservation, breakdown, paymentResult, failedMsg);
        }
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public long getConfirmedReservationCount() {
        return reservationRepository.countConfirmed();
    }
}
