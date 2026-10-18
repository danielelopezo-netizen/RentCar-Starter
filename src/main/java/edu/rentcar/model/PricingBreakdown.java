package edu.rentcar.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Detailed cost breakdown for a rental request (RF-002, RF-003, RF-005, RF-007).
 * Uses BigDecimal with HALF_UP rounding to 2 decimal places.
 */
public final class PricingBreakdown {
    private final long rentalDays;
    private final BigDecimal baseVehicleCost;
    private final BigDecimal modalityFee;
    private final BigDecimal coverageCost;
    private final BigDecimal assistanceCost;
    private final BigDecimal singleServicesCost;
    private final BigDecimal packagesCost;
    private final BigDecimal totalCost;
    private final String fullDescription;
    private final List<String> skippedComplimentaryServices;

    public PricingBreakdown(long rentalDays,
                            BigDecimal baseVehicleCost,
                            BigDecimal modalityFee,
                            BigDecimal coverageCost,
                            BigDecimal assistanceCost,
                            BigDecimal singleServicesCost,
                            BigDecimal packagesCost,
                            String fullDescription,
                            List<String> skippedComplimentaryServices) {
        this.rentalDays = rentalDays;
        this.baseVehicleCost = scale(baseVehicleCost);
        this.modalityFee = scale(modalityFee);
        this.coverageCost = scale(coverageCost);
        this.assistanceCost = scale(assistanceCost);
        this.singleServicesCost = scale(singleServicesCost);
        this.packagesCost = scale(packagesCost);
        this.totalCost = this.baseVehicleCost
                .add(this.modalityFee)
                .add(this.coverageCost)
                .add(this.assistanceCost)
                .add(this.singleServicesCost)
                .add(this.packagesCost)
                .setScale(2, RoundingMode.HALF_UP);
        this.fullDescription = Objects.requireNonNullElse(fullDescription, "");
        this.skippedComplimentaryServices = skippedComplimentaryServices == null
                ? List.of()
                : Collections.unmodifiableList(skippedComplimentaryServices);
    }

    private static BigDecimal scale(BigDecimal value) {
        return Objects.requireNonNullElse(value, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public long getRentalDays() {
        return rentalDays;
    }

    public BigDecimal getBaseVehicleCost() {
        return baseVehicleCost;
    }

    public BigDecimal getModalityFee() {
        return modalityFee;
    }

    public BigDecimal getCoverageCost() {
        return coverageCost;
    }

    public BigDecimal getAssistanceCost() {
        return assistanceCost;
    }

    /**
     * Returns combined cost of modality fee + coverage + assistance.
     */
    public BigDecimal getTotalModalityCost() {
        return modalityFee.add(coverageCost).add(assistanceCost).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getSingleServicesCost() {
        return singleServicesCost;
    }

    public BigDecimal getPackagesCost() {
        return packagesCost;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public List<String> getSkippedComplimentaryServices() {
        return skippedComplimentaryServices;
    }
}
