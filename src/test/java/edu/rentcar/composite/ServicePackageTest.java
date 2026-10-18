package edu.rentcar.composite;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RF-007 (Composite Pattern: SingleService and ServicePackage).
 */
class ServicePackageTest {

    @Test
    void simplePackageShouldCalculateSumOfLeafServices() {
        ServicePackage familyPkg = ServiceCatalog.familyPackage(); // GPS (25,000/day) + Child Seat (20,000/day)

        // For 4 days: (25,000 + 20,000) * 4 = 180,000.00
        assertEquals(new BigDecimal("180000.00"), familyPkg.calculateCost(4L));
        assertTrue(familyPkg.getDescription().contains("GPS Navigation"));
        assertTrue(familyPkg.getDescription().contains("Child Safety Seat"));
        assertEquals(2, familyPkg.getIncludedServiceCodes().size());
    }

    @Test
    void nestedPackageShouldCalculateCostRecursively() {
        // All-Inclusive Touring Package contains Family Package (GPS + Child Seat) + Additional Driver + Wi-Fi + Toll Pass
        ServicePackage allInclusive = ServiceCatalog.allInclusiveTouringPackage();

        // For 3 days:
        // Family Package: (25,000 + 20,000) * 3 = 135,000
        // Additional Driver: 30,000 * 3 = 90,000
        // Wi-Fi: 18,000 * 3 = 54,000
        // Toll Pass (flat): 35,000
        // Total = 314,000.00
        assertEquals(new BigDecimal("314000.00"), allInclusive.calculateCost(3L));
        assertEquals(5, allInclusive.getIncludedServiceCodes().size());
        assertTrue(allInclusive.getDescription().contains("Family Comfort Package"));
    }

    @Test
    void packageWithDiscountShouldApplyPercentageCorrectly() {
        ServicePackage discountedPkg = new ServicePackage("PKG-PROMO", "Promo Bundle", BigDecimal.valueOf(10))
                .addComponent(ServiceCatalog.gpsService())       // 25,000 * 2 = 50,000
                .addComponent(ServiceCatalog.childSeatService()); // 20,000 * 2 = 40,000

        // Gross = 90,000; 10% discount => 81,000.00
        assertEquals(new BigDecimal("81000.00"), discountedPkg.calculateCost(2L));
    }

    @Test
    void addingSelfOrCircularPackageShouldThrowException() {
        ServicePackage pkgA = new ServicePackage("PKG-A", "Package A");
        ServicePackage pkgB = new ServicePackage("PKG-B", "Package B");
        pkgA.addComponent(pkgB);

        assertThrows(IllegalArgumentException.class, () -> pkgA.addComponent(pkgA));
        assertThrows(IllegalArgumentException.class, () -> pkgB.addComponent(pkgA));
    }

    @Test
    void addingDuplicateServiceInsidePackageShouldThrowException() {
        ServicePackage pkg = new ServicePackage("PKG-DUP", "Duplicate Test");
        pkg.addComponent(ServiceCatalog.gpsService());

        assertThrows(IllegalArgumentException.class, () -> pkg.addComponent(ServiceCatalog.gpsService()));
    }
}
