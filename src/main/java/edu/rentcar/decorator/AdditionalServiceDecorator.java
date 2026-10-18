package edu.rentcar.decorator;

import edu.rentcar.composite.ServiceComponent;
import edu.rentcar.composite.ServicePackage;
import edu.rentcar.composite.SingleService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Concrete Decorator in the Decorator pattern (RF-005).
 * Dynamically adds any ServiceComponent (individual SingleService or composite ServicePackage)
 * to a BillableRental while avoiding double-charging for already included or duplicated services.
 */
public class AdditionalServiceDecorator extends RentalDecorator {
    private final ServiceComponent serviceComponent;
    private final BigDecimal addedCost;
    private final boolean complimentaryOrDuplicate;

    public AdditionalServiceDecorator(BillableRental wrappedRental, ServiceComponent serviceComponent) {
        super(wrappedRental);
        this.serviceComponent = Objects.requireNonNull(serviceComponent, "Service component cannot be null.");
        Set<String> alreadyApplied = wrappedRental.getAppliedServiceCodes();
        this.addedCost = computeEffectiveCost(serviceComponent, alreadyApplied, wrappedRental.getRentalDays());
        this.complimentaryOrDuplicate = this.addedCost.compareTo(BigDecimal.ZERO) == 0;
    }

    public ServiceComponent getServiceComponent() {
        return serviceComponent;
    }

    public BigDecimal getAddedCost() {
        return addedCost;
    }

    public boolean isComplimentaryOrDuplicate() {
        return complimentaryOrDuplicate;
    }

    @Override
    public String getDescription() {
        if (complimentaryOrDuplicate) {
            return wrappedRental.getDescription() + " + " + serviceComponent.getName() + " (Included)";
        }
        return wrappedRental.getDescription() + " + " + serviceComponent.getDescription();
    }

    @Override
    public BigDecimal calculateCost() {
        return wrappedRental.calculateCost().add(addedCost).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Set<String> getAppliedServiceCodes() {
        Set<String> merged = new LinkedHashSet<>(wrappedRental.getAppliedServiceCodes());
        merged.addAll(serviceComponent.getIncludedServiceCodes());
        return Collections.unmodifiableSet(merged);
    }

    private static BigDecimal computeEffectiveCost(ServiceComponent component,
                                                   Set<String> alreadyApplied,
                                                   long rentalDays) {
        if (component instanceof SingleService single) {
            if (alreadyApplied.contains(single.getCode())) {
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
            return single.calculateCost(rentalDays);
        }
        if (component instanceof ServicePackage pkg) {
            BigDecimal sum = BigDecimal.ZERO;
            Set<String> localApplied = new LinkedHashSet<>(alreadyApplied);
            for (ServiceComponent child : pkg.getComponents()) {
                BigDecimal childCost = computeEffectiveCost(child, localApplied, rentalDays);
                sum = sum.add(childCost);
                localApplied.addAll(child.getIncludedServiceCodes());
            }
            if (pkg.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0 && sum.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal multiplier = BigDecimal.valueOf(100).subtract(pkg.getDiscountPercentage())
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                return sum.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
            }
            return sum.setScale(2, RoundingMode.HALF_UP);
        }
        // Fallback for custom ServiceComponent implementations
        if (alreadyApplied.containsAll(component.getIncludedServiceCodes())) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return component.calculateCost(rentalDays);
    }
}
