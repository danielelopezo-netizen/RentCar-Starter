package edu.rentcar.service;

import edu.rentcar.composite.ServiceComponent;
import edu.rentcar.composite.ServicePackage;
import edu.rentcar.composite.SingleService;
import edu.rentcar.decorator.AdditionalDriverDecorator;
import edu.rentcar.decorator.AdditionalServiceDecorator;
import edu.rentcar.decorator.BaseVehicleRental;
import edu.rentcar.decorator.BillableRental;
import edu.rentcar.decorator.ChildSeatDecorator;
import edu.rentcar.decorator.GpsServiceDecorator;
import edu.rentcar.factory.AssistancePolicy;
import edu.rentcar.factory.CoveragePolicy;
import edu.rentcar.factory.RatePolicy;
import edu.rentcar.factory.RentalModeFactory;
import edu.rentcar.factory.RentalModeFactoryProvider;
import edu.rentcar.model.PricingBreakdown;
import edu.rentcar.model.ReservationRequest;
import edu.rentcar.composite.ServiceCatalog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Coordinates rental cost calculation by integrating Abstract Factory (RF-003),
 * Decorator (RF-005), and Composite (RF-007) patterns.
 */
public class PricingService {

    /**
     * Builds the decorated BillableRental chain for a given ReservationRequest.
     */
    public BillableRental buildDecoratedRental(ReservationRequest request) {
        Objects.requireNonNull(request, "ReservationRequest is required.");
        RentalModeFactory factory = RentalModeFactoryProvider.getFactory(request.getRentalMode());

        RatePolicy ratePolicy = factory.createRatePolicy();
        CoveragePolicy coveragePolicy = factory.createCoveragePolicy();
        AssistancePolicy assistancePolicy = factory.createAssistancePolicy();

        BillableRental rental = new BaseVehicleRental(
                request.getVehicle(),
                request.getRentalDays(),
                request.getRentalMode(),
                ratePolicy,
                coveragePolicy,
                assistancePolicy
        );

        for (ServiceComponent component : request.getSelectedServices()) {
            rental = wrapWithDecorator(rental, component);
        }

        return rental;
    }

    /**
     * Computes a detailed PricingBreakdown for the UI and reservation process.
     */
    public PricingBreakdown calculateBreakdown(ReservationRequest request) {
        Objects.requireNonNull(request, "ReservationRequest is required.");
        RentalModeFactory factory = RentalModeFactoryProvider.getFactory(request.getRentalMode());

        RatePolicy ratePolicy = factory.createRatePolicy();
        CoveragePolicy coveragePolicy = factory.createCoveragePolicy();
        AssistancePolicy assistancePolicy = factory.createAssistancePolicy();

        long days = request.getRentalDays();
        BaseVehicleRental baseRental = new BaseVehicleRental(
                request.getVehicle(),
                days,
                request.getRentalMode(),
                ratePolicy,
                coveragePolicy,
                assistancePolicy
        );

        BigDecimal baseVehicleCost = baseRental.getBaseVehicleCost();
        BigDecimal modalityFee = baseRental.getModalityFee();
        BigDecimal coverageCost = baseRental.getCoverageCost();
        BigDecimal assistanceCost = baseRental.getAssistanceCost();

        BigDecimal singleServicesCost = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal packagesCost = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        List<String> skippedComplimentary = new ArrayList<>();

        BillableRental currentRental = baseRental;
        for (ServiceComponent component : request.getSelectedServices()) {
            AdditionalServiceDecorator decorated = wrapWithDecorator(currentRental, component);
            BigDecimal added = decorated.getAddedCost();
            if (decorated.isComplimentaryOrDuplicate()) {
                skippedComplimentary.add(component.getName());
            } else if (component instanceof ServicePackage) {
                packagesCost = packagesCost.add(added).setScale(2, RoundingMode.HALF_UP);
            } else {
                singleServicesCost = singleServicesCost.add(added).setScale(2, RoundingMode.HALF_UP);
            }
            currentRental = decorated;
        }

        return new PricingBreakdown(
                days,
                baseVehicleCost,
                modalityFee,
                coverageCost,
                assistanceCost,
                singleServicesCost,
                packagesCost,
                currentRental.getDescription(),
                skippedComplimentary
        );
    }

    private AdditionalServiceDecorator wrapWithDecorator(BillableRental current, ServiceComponent component) {
        if (component instanceof SingleService single) {
            return switch (single.getCode()) {
                case ServiceCatalog.CODE_GPS -> new GpsServiceDecorator(current);
                case ServiceCatalog.CODE_CHILD_SEAT -> new ChildSeatDecorator(current);
                case ServiceCatalog.CODE_ADDITIONAL_DRIVER -> new AdditionalDriverDecorator(current);
                default -> new AdditionalServiceDecorator(current, single);
            };
        }
        return new AdditionalServiceDecorator(current, component);
    }
}
