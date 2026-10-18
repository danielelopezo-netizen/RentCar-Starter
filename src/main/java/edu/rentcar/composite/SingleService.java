package edu.rentcar.composite;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Set;

/**
 * Leaf participant in the Composite pattern representing an individual optional service (RF-005, RF-007).
 */
public final class SingleService implements ServiceComponent {
    private final String code;
    private final String name;
    private final String description;
    private final BigDecimal unitPrice;
    private final boolean dailyRate;

    public SingleService(String code, String name, String description, BigDecimal unitPrice, boolean dailyRate) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Service code is required.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Service name is required.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Service unit price cannot be null or negative.");
        }
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.description = description == null ? this.name : description.trim();
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
        this.dailyRate = dailyRate;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    @Override
    public boolean isDailyRate() {
        return dailyRate;
    }

    @Override
    public BigDecimal calculateCost(long rentalDays) {
        if (rentalDays <= 0) {
            throw new IllegalArgumentException("Rental days must be greater than zero.");
        }
        if (dailyRate) {
            return unitPrice.multiply(BigDecimal.valueOf(rentalDays)).setScale(2, RoundingMode.HALF_UP);
        }
        return unitPrice;
    }

    @Override
    public Set<String> getIncludedServiceCodes() {
        return Set.of(code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SingleService that)) return false;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return name + " (" + (dailyRate ? unitPrice + " COP/day" : unitPrice + " COP flat") + ")";
    }
}
