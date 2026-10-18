package edu.rentcar.model;

/**
 * Explicit lifecycle states for a vehicle rental reservation (RF-002).
 */
public enum ReservationStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    REJECTED("Rejected"),
    FAILED("Failed");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
