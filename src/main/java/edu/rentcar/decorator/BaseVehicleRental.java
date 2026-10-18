package edu.rentcar.decorator;

import edu.rentcar.factory.AssistancePolicy;
import edu.rentcar.factory.CoveragePolicy;
import edu.rentcar.factory.RatePolicy;
import edu.rentcar.model.RentalMode;
import edu.rentcar.model.Vehicle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Concrete Component in the Decorator pattern (RF-005).
 * Represents the base vehicle rental configured with the policies produced by the Abstract Factory (RF-003).
 */
public final class BaseVehicleRental implements BillableRental {
    private final Vehicle vehicle;
    private final long rentalDays;
    private final RentalMode rentalMode;
    private final RatePolicy ratePolicy;
    private final CoveragePolicy coveragePolicy;
    private final AssistancePolicy assistancePolicy;

    public BaseVehicleRental(Vehicle vehicle,
                             long rentalDays,
                             RentalMode rentalMode,
                             RatePolicy ratePolicy,
                             CoveragePolicy coveragePolicy,
                             AssistancePolicy assistancePolicy) {
        this.vehicle = Objects.requireNonNull(vehicle, "Vehicle is required.");
        if (rentalDays <= 0) {
            throw new IllegalArgumentException("Rental days must be positive.");
        }
        this.rentalDays = rentalDays;
        this.rentalMode = Objects.requireNonNull(rentalMode, "Rental mode is required.");
        this.ratePolicy = Objects.requireNonNull(ratePolicy, "Rate policy is required.");
        this.coveragePolicy = Objects.requireNonNull(coveragePolicy, "Coverage policy is required.");
        this.assistancePolicy = Objects.requireNonNull(assistancePolicy, "Assistance policy is required.");
    }

    public BigDecimal getBaseVehicleCost() {
        return ratePolicy.calculateBaseVehicleCost(BigDecimal.valueOf(vehicle.getDailyRate()), rentalDays);
    }

    public BigDecimal getModalityFee() {
        return ratePolicy.calculateModalityFee(BigDecimal.valueOf(vehicle.getDailyRate()), rentalDays);
    }

    public BigDecimal getCoverageCost() {
        return coveragePolicy.calculateCoverageCost(rentalDays);
    }

    public BigDecimal getAssistanceCost() {
        return assistancePolicy.calculateAssistanceCost(rentalDays);
    }

    @Override
    public long getRentalDays() {
        return rentalDays;
    }

    @Override
    public String getDescription() {
        return String.format(
                "%s (%d days, %s mode: %s, %s, %s)",
                vehicle.getDisplayName(),
                rentalDays,
                rentalMode.getDisplayName(),
                ratePolicy.getPolicyName(),
                coveragePolicy.getCoverageName(),
                assistancePolicy.getAssistanceName()
        );
    }

    @Override
    public BigDecimal calculateCost() {
        return getBaseVehicleCost()
                .add(getModalityFee())
                .add(getCoverageCost())
                .add(getAssistanceCost())
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Set<String> getAppliedServiceCodes() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(coveragePolicy.getIncludedServiceCodes()));
    }
}
