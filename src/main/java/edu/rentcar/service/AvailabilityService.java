package edu.rentcar.service;

import edu.rentcar.model.Reservation;
import edu.rentcar.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Centralized service for evaluating vehicle availability and detecting reservation interval overlaps (RF-002).
 *
 * Interval overlap rule:
 * A rental interval [start, end) occupies the days from start (pick-up) to end (return).
 * Two intervals [startA, endA) and [startB, endB) overlap if and only if startA.isBefore(endB) && startB.isBefore(endA).
 * Consequently, consecutive rentals where endA.equals(startB) do NOT overlap and are allowed.
 * Only CONFIRMED reservations block vehicle availability; PENDING, REJECTED, and FAILED reservations do not.
 */
public class AvailabilityService {
    private final ReservationRepository reservationRepository;

    public AvailabilityService(ReservationRepository reservationRepository) {
        this.reservationRepository = Objects.requireNonNull(reservationRepository, "ReservationRepository is required.");
    }

    public boolean isVehicleAvailable(String vehicleId, LocalDate startDate, LocalDate endDate) {
        validateInterval(startDate, endDate);
        if (vehicleId == null || vehicleId.isBlank()) {
            throw new IllegalArgumentException("Vehicle ID is required to check availability.");
        }

        List<Reservation> confirmedReservations = reservationRepository.findConfirmedByVehicleId(vehicleId);
        for (Reservation existing : confirmedReservations) {
            if (intervalsOverlap(startDate, endDate, existing.getStartDate(), existing.getEndDate())) {
                return false;
            }
        }
        return true;
    }

    public static boolean intervalsOverlap(LocalDate startA, LocalDate endA, LocalDate startB, LocalDate endB) {
        validateInterval(startA, endA);
        validateInterval(startB, endB);
        return startA.isBefore(endB) && startB.isBefore(endA);
    }

    private static void validateInterval(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required.");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date must be strictly after start date.");
        }
    }
}
