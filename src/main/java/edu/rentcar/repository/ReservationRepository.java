package edu.rentcar.repository;

import edu.rentcar.model.Reservation;
import edu.rentcar.model.ReservationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe in-memory repository for managing rental reservations (RF-002).
 */
public class ReservationRepository {
    private final List<Reservation> reservations = new ArrayList<>();
    private final AtomicInteger sequence = new AtomicInteger(1001);

    public String nextReservationId() {
        return "RES-" + sequence.getAndIncrement();
    }

    public synchronized Reservation save(Reservation reservation) {
        Objects.requireNonNull(reservation, "Reservation cannot be null.");
        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getId().equals(reservation.getId())) {
                reservations.set(i, reservation);
                return reservation;
            }
        }
        reservations.add(reservation);
        return reservation;
    }

    public synchronized List<Reservation> findAll() {
        return List.copyOf(reservations);
    }

    public synchronized Optional<Reservation> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return reservations.stream()
                .filter(r -> r.getId().equalsIgnoreCase(id.trim()))
                .findFirst();
    }

    public synchronized List<Reservation> findConfirmedByVehicleId(String vehicleId) {
        if (vehicleId == null || vehicleId.isBlank()) {
            return List.of();
        }
        return reservations.stream()
                .filter(r -> r.getVehicleId().equalsIgnoreCase(vehicleId.trim()))
                .filter(r -> r.getReservationStatus() == ReservationStatus.CONFIRMED)
                .toList();
    }

    public synchronized int count() {
        return reservations.size();
    }

    public synchronized long countConfirmed() {
        return reservations.stream()
                .filter(Reservation::isConfirmed)
                .count();
    }
}
