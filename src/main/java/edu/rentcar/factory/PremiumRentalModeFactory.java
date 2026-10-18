package edu.rentcar.factory;

import edu.rentcar.model.RentalMode;

/**
 * Concrete Factory for the PREMIUM rental modality (RF-003).
 */
public final class PremiumRentalModeFactory implements RentalModeFactory {

    @Override
    public RentalMode getRentalMode() {
        return RentalMode.PREMIUM;
    }

    @Override
    public CoveragePolicy createCoveragePolicy() {
        return new PremiumCoveragePolicy();
    }

    @Override
    public RatePolicy createRatePolicy() {
        return new PremiumRatePolicy();
    }

    @Override
    public AssistancePolicy createAssistancePolicy() {
        return new PremiumAssistancePolicy();
    }
}
