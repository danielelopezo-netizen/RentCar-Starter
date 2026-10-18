package edu.rentcar.factory;

import edu.rentcar.composite.ServiceCatalog;
import edu.rentcar.model.Customer;
import edu.rentcar.model.PricingBreakdown;
import edu.rentcar.model.RentalMode;
import edu.rentcar.model.ReservationRequest;
import edu.rentcar.model.Vehicle;
import edu.rentcar.builder.ReservationRequestBuilder;
import edu.rentcar.service.PricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RF-003 (Abstract Factory Pattern: RentalModeFactory and tariff calculations).
 */
class RentalModeFactoryTest {

    private PricingService pricingService;
    private Customer customer;
    private Vehicle vehicle;
    private LocalDate start;
    private LocalDate end;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
        customer = new Customer("CUS-1001", "Sofia Martinez", "sofia@example.com", true);
        // 150,000 COP/day, 3 days => Base vehicle cost = 450,000 COP
        vehicle = new Vehicle("VEH-1001", "Toyota", "Corolla", "Economy", 2024, 150000, "Available");
        start = LocalDate.of(2026, 10, 20);
        end = LocalDate.of(2026, 10, 23);
    }

    @Test
    void basicModalityShouldCalculateExactBaseCostWithNoModalitySurcharge() {
        ReservationRequest request = new ReservationRequestBuilder()
                .withCustomer(customer)
                .withVehicle(vehicle)
                .withDates(start, end)
                .withRentalMode(RentalMode.BASIC)
                .build();

        PricingBreakdown breakdown = pricingService.calculateBreakdown(request);

        assertEquals(new BigDecimal("450000.00"), breakdown.getBaseVehicleCost());
        assertEquals(new BigDecimal("0.00"), breakdown.getModalityFee());
        assertEquals(new BigDecimal("0.00"), breakdown.getCoverageCost());
        assertEquals(new BigDecimal("0.00"), breakdown.getAssistanceCost());
        assertEquals(new BigDecimal("450000.00"), breakdown.getTotalCost());
    }

    @Test
    void standardModalityShouldCalculateSurchargeCoverageAndAssistanceAndIncludeGps() {
        ReservationRequest request = new ReservationRequestBuilder()
                .withCustomer(customer)
                .withVehicle(vehicle)
                .withDates(start, end)
                .withRentalMode(RentalMode.STANDARD)
                .addService(ServiceCatalog.gpsService()) // GPS is complimentary in STANDARD!
                .build();

        PricingBreakdown breakdown = pricingService.calculateBreakdown(request);

        // Base = 450,000
        // Modality fee (10%) = 45,000
        // Coverage (25,000 * 3) = 75,000
        // Assistance (10,000 * 3) = 30,000
        // GPS is included in STANDARD => 0 additional service cost
        // Total = 600,000.00
        assertEquals(new BigDecimal("450000.00"), breakdown.getBaseVehicleCost());
        assertEquals(new BigDecimal("45000.00"), breakdown.getModalityFee());
        assertEquals(new BigDecimal("75000.00"), breakdown.getCoverageCost());
        assertEquals(new BigDecimal("30000.00"), breakdown.getAssistanceCost());
        assertEquals(new BigDecimal("0.00"), breakdown.getSingleServicesCost());
        assertEquals(new BigDecimal("600000.00"), breakdown.getTotalCost());
        assertTrue(breakdown.getSkippedComplimentaryServices().contains("GPS Navigation"));
    }

    @Test
    void premiumModalityShouldCalculateFullBenefitsAndIncludeGpsAndAdditionalDriver() {
        ReservationRequest request = new ReservationRequestBuilder()
                .withCustomer(customer)
                .withVehicle(vehicle)
                .withDates(start, end)
                .withRentalMode(RentalMode.PREMIUM)
                .addService(ServiceCatalog.gpsService())
                .addService(ServiceCatalog.additionalDriverService())
                .addService(ServiceCatalog.childSeatService()) // 20,000 * 3 = 60,000 extra
                .build();

        PricingBreakdown breakdown = pricingService.calculateBreakdown(request);

        // Base = 450,000
        // Modality fee (20%) = 90,000
        // Coverage (45,000 * 3) = 135,000
        // Assistance (20,000 * 3) = 60,000
        // GPS & Additional Driver = 0 (included in PREMIUM)
        // Child Seat = 60,000
        // Total = 795,000.00
        assertEquals(new BigDecimal("450000.00"), breakdown.getBaseVehicleCost());
        assertEquals(new BigDecimal("90000.00"), breakdown.getModalityFee());
        assertEquals(new BigDecimal("135000.00"), breakdown.getCoverageCost());
        assertEquals(new BigDecimal("60000.00"), breakdown.getAssistanceCost());
        assertEquals(new BigDecimal("60000.00"), breakdown.getSingleServicesCost());
        assertEquals(new BigDecimal("795000.00"), breakdown.getTotalCost());
    }

    @Test
    void factoryProviderShouldReturnDistinctCoherentPolicyFamilies() {
        RentalModeFactory basic = RentalModeFactoryProvider.getFactory(RentalMode.BASIC);
        RentalModeFactory standard = RentalModeFactoryProvider.getFactory(RentalMode.STANDARD);
        RentalModeFactory premium = RentalModeFactoryProvider.getFactory(RentalMode.PREMIUM);

        assertInstanceOf(BasicRatePolicy.class, basic.createRatePolicy());
        assertInstanceOf(StandardRatePolicy.class, standard.createRatePolicy());
        assertInstanceOf(PremiumRatePolicy.class, premium.createRatePolicy());

        assertTrue(basic.createCoveragePolicy().getIncludedServiceCodes().isEmpty());
        assertTrue(standard.createCoveragePolicy().getIncludedServiceCodes().contains(ServiceCatalog.CODE_GPS));
        assertTrue(premium.createCoveragePolicy().getIncludedServiceCodes().contains(ServiceCatalog.CODE_ADDITIONAL_DRIVER));
    }
}
