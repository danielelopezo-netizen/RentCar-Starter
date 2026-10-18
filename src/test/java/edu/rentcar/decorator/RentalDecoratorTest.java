package edu.rentcar.decorator;

import edu.rentcar.composite.ServiceCatalog;
import edu.rentcar.factory.RentalModeFactory;
import edu.rentcar.factory.RentalModeFactoryProvider;
import edu.rentcar.model.RentalMode;
import edu.rentcar.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RF-005 (Decorator Pattern: BillableRental and concrete service decorators).
 */
class RentalDecoratorTest {

    private BillableRental baseRental;

    @BeforeEach
    void setUp() {
        Vehicle vehicle = new Vehicle("VEH-1001", "Toyota", "Corolla", "Economy", 2024, 150000, "Available");
        RentalModeFactory basicFactory = RentalModeFactoryProvider.getFactory(RentalMode.BASIC);
        // 2 days * 150,000 = 300,000 COP
        baseRental = new BaseVehicleRental(
                vehicle,
                2L,
                RentalMode.BASIC,
                basicFactory.createRatePolicy(),
                basicFactory.createCoveragePolicy(),
                basicFactory.createAssistancePolicy()
        );
    }

    @Test
    void singleDecoratorShouldAddCostAndExtendDescription() {
        BillableRental withGps = new GpsServiceDecorator(baseRental);

        // 300,000 + (25,000 * 2) = 350,000.00
        assertEquals(new BigDecimal("350000.00"), withGps.calculateCost());
        assertTrue(withGps.getDescription().contains("Satellite GPS navigation unit"));
    }

    @Test
    void multipleCombinedDecoratorsShouldAccumulateCostsAndDescriptions() {
        BillableRental decorated = new GpsServiceDecorator(baseRental);
        decorated = new ChildSeatDecorator(decorated);
        decorated = new AdditionalDriverDecorator(decorated);
        decorated = new AdditionalServiceDecorator(decorated, ServiceCatalog.tollPassService());

        // Base: 300,000
        // GPS: 25,000 * 2 = 50,000
        // Child Seat: 20,000 * 2 = 40,000
        // Additional Driver: 30,000 * 2 = 60,000
        // Toll Pass (flat): 35,000
        // Total = 485,000.00
        assertEquals(new BigDecimal("485000.00"), decorated.calculateCost());
        assertTrue(decorated.getDescription().contains("Satellite GPS navigation unit"));
        assertTrue(decorated.getDescription().contains("Certified child safety seat"));
        assertTrue(decorated.getDescription().contains("Authorization for a second driver"));
        assertTrue(decorated.getDescription().contains("Electronic toll tag activation fee"));
    }

    @Test
    void accidentalDuplicateDecoratorShouldNotDoubleCharge() {
        BillableRental decoratedOnce = new GpsServiceDecorator(baseRental);
        BillableRental decoratedTwice = new GpsServiceDecorator(decoratedOnce);

        assertEquals(new BigDecimal("350000.00"), decoratedOnce.calculateCost());
        assertEquals(new BigDecimal("350000.00"), decoratedTwice.calculateCost());
        assertTrue(decoratedTwice.getDescription().contains("(Included)"));
    }
}
