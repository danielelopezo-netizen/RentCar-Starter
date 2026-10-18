package edu.rentcar.factory;

import edu.rentcar.model.RentalMode;

/**
 * Abstract Factory interface (RF-003) responsible for creating coherent families of
 * rate, coverage, and assistance policies according to the selected rental modality.
 */
public interface RentalModeFactory {

    RentalMode getRentalMode();

    RatePolicy createRatePolicy();

    CoveragePolicy createCoveragePolicy();

    AssistancePolicy createAssistancePolicy();
}
