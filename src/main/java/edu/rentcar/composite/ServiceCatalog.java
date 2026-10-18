package edu.rentcar.composite;

import java.math.BigDecimal;
import java.util.List;

/**
 * Central catalog of individual optional services and preconfigured packages (RF-005, RF-007).
 * Centralizes demonstration prices to avoid scattering monetary literals across controllers or views.
 */
public final class ServiceCatalog {
    public static final String CODE_GPS = "SRV-GPS";
    public static final String CODE_CHILD_SEAT = "SRV-CHILD-SEAT";
    public static final String CODE_ADDITIONAL_DRIVER = "SRV-ADD-DRIVER";
    public static final String CODE_WIFI = "SRV-WIFI";
    public static final String CODE_TOLL_PASS = "SRV-TOLL-PASS";

    public static final String CODE_FAMILY_PKG = "PKG-FAMILY";
    public static final String CODE_EXECUTIVE_PKG = "PKG-EXECUTIVE";
    public static final String CODE_ALL_INCLUSIVE_PKG = "PKG-ALL-INCLUSIVE";

    private ServiceCatalog() {
    }

    public static SingleService gpsService() {
        return new SingleService(
                CODE_GPS,
                "GPS Navigation",
                "Satellite GPS navigation unit",
                BigDecimal.valueOf(25000),
                true
        );
    }

    public static SingleService childSeatService() {
        return new SingleService(
                CODE_CHILD_SEAT,
                "Child Safety Seat",
                "Certified child safety seat",
                BigDecimal.valueOf(20000),
                true
        );
    }

    public static SingleService additionalDriverService() {
        return new SingleService(
                CODE_ADDITIONAL_DRIVER,
                "Additional Driver",
                "Authorization for a second driver",
                BigDecimal.valueOf(30000),
                true
        );
    }

    public static SingleService portableWifiService() {
        return new SingleService(
                CODE_WIFI,
                "Portable Wi-Fi Hotspot",
                "Unlimited 4G/5G in-car Wi-Fi hotspot",
                BigDecimal.valueOf(18000),
                true
        );
    }

    public static SingleService tollPassService() {
        return new SingleService(
                CODE_TOLL_PASS,
                "Electronic Toll Pass",
                "Electronic toll tag activation fee (flat per rental)",
                BigDecimal.valueOf(35000),
                false
        );
    }

    public static ServicePackage familyPackage() {
        return new ServicePackage(CODE_FAMILY_PKG, "Family Comfort Package")
                .addComponent(gpsService())
                .addComponent(childSeatService());
    }

    public static ServicePackage executivePackage() {
        return new ServicePackage(CODE_EXECUTIVE_PKG, "Executive Mobility Package")
                .addComponent(additionalDriverService())
                .addComponent(portableWifiService())
                .addComponent(tollPassService());
    }

    /**
     * Nested package combining the Family Comfort Package with Executive services.
     */
    public static ServicePackage allInclusiveTouringPackage() {
        return new ServicePackage(CODE_ALL_INCLUSIVE_PKG, "All-Inclusive Touring Package")
                .addComponent(familyPackage())
                .addComponent(additionalDriverService())
                .addComponent(portableWifiService())
                .addComponent(tollPassService());
    }

    public static List<SingleService> availableSingleServices() {
        return List.of(
                gpsService(),
                childSeatService(),
                additionalDriverService(),
                portableWifiService(),
                tollPassService()
        );
    }

    public static List<ServicePackage> availablePackages() {
        return List.of(
                familyPackage(),
                executivePackage(),
                allInclusiveTouringPackage()
        );
    }
}
