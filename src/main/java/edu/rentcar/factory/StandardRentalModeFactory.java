package edu.rentcar.factory;

import edu.rentcar.model.RentalMode;

/**
 * Concrete Factory for the STANDARD rental modality (RF-003).
 */
public final class StandardRentalModeFactory implements RentalModeFactory {

    @Override
    public RentalMode getRentalMode() {
        return RentalMode.STANDARD;
    }

    @Override
    public RatePolicy createRatePolicy() {
        return new StandardRatePolicy();
    }

    @Override
    public CoveragePolicy createCoveragePolicy() {
        return new StandardCoveragePolicy();
    }

    @Override
    public AssistancePolicy createAssistancePolicy() {
        return new StandardAssistancePolicy();
    }
}
