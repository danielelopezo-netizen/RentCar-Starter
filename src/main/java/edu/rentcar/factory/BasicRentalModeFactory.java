package edu.rentcar.factory;

import edu.rentcar.model.RentalMode;

/**
 * Concrete Factory for the BASIC rental modality (RF-003).
 */
public final class BasicRentalModeFactory implements RentalModeFactory {

    @Override
    public RentalMode getRentalMode() {
        return RentalMode.BASIC;
    }

    @Override
    public RatePolicy createRatePolicy() {
        return new BasicRatePolicy();
    }

    @Override
    public CoveragePolicy createCoveragePolicy() {
        return new BasicCoveragePolicy();
    }

    @Override
    public AssistancePolicy createAssistancePolicy() {
        return new BasicAssistancePolicy();
    }
}
