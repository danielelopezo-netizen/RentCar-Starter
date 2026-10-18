package edu.rentcar.model;

/**
 * Available rental modalities with differentiated pricing, coverage, and assistance policies (RF-003).
 */
public enum RentalMode {
    BASIC("Basic", "Economical coverage with essential protection"),
    STANDARD("Standard", "Balanced protection with roadside assistance and GPS included"),
    PREMIUM("Premium", "All-inclusive protection, full roadside assistance, GPS and additional driver included");

    private final String displayName;
    private final String summary;

    RentalMode(String displayName, String summary) {
        this.displayName = displayName;
        this.summary = summary;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSummary() {
        return summary;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
