package edu.rentcar.composite;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Composite participant in the Composite pattern representing a bundle of services or sub-packages (RF-007).
 * Prevents circular references and duplicate leaf components within the hierarchy.
 */
public final class ServicePackage implements ServiceComponent {
    private final String code;
    private final String name;
    private final BigDecimal discountPercentage;
    private final List<ServiceComponent> components = new ArrayList<>();

    public ServicePackage(String code, String name) {
        this(code, name, BigDecimal.ZERO);
    }

    public ServicePackage(String code, String name, BigDecimal discountPercentage) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Package code is required.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Package name is required.");
        }
        if (discountPercentage == null
                || discountPercentage.compareTo(BigDecimal.ZERO) < 0
                || discountPercentage.compareTo(BigDecimal.valueOf(100)) >= 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and less than 100.");
        }
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.discountPercentage = discountPercentage;
    }

    public ServicePackage addComponent(ServiceComponent component) {
        Objects.requireNonNull(component, "Cannot add a null component to a service package.");
        if (this.equals(component) || containsHierarchyReference(component, this.code)) {
            throw new IllegalArgumentException(
                    "Circular or self reference detected when adding '" + component.getCode() + "' to '" + this.code + "'."
            );
        }
        // Prevent duplicate component codes inside the same package
        for (String incomingCode : component.getIncludedServiceCodes()) {
            if (getIncludedServiceCodes().contains(incomingCode)) {
                throw new IllegalArgumentException(
                        "Duplicate service '" + incomingCode + "' is already included in package '" + this.code + "'."
                );
            }
        }
        components.add(component);
        return this;
    }

    public boolean removeComponent(ServiceComponent component) {
        return components.remove(component);
    }

    public List<ServiceComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        if (components.isEmpty()) {
            return name + " (Empty Package)";
        }
        String childrenNames = components.stream()
                .map(ServiceComponent::getName)
                .collect(Collectors.joining(" + "));
        if (discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            return name + " [" + childrenNames + " | -" + discountPercentage.stripTrailingZeros().toPlainString() + "%]";
        }
        return name + " [" + childrenNames + "]";
    }

    @Override
    public boolean isDailyRate() {
        return components.stream().anyMatch(ServiceComponent::isDailyRate);
    }

    @Override
    public BigDecimal calculateCost(long rentalDays) {
        if (rentalDays <= 0) {
            throw new IllegalArgumentException("Rental days must be greater than zero.");
        }
        if (components.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal grossTotal = BigDecimal.ZERO;
        for (ServiceComponent component : components) {
            grossTotal = grossTotal.add(component.calculateCost(rentalDays));
        }
        if (discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal multiplier = BigDecimal.valueOf(100).subtract(discountPercentage)
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            return grossTotal.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        }
        return grossTotal.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Set<String> getIncludedServiceCodes() {
        Set<String> codes = new LinkedHashSet<>();
        for (ServiceComponent component : components) {
            codes.addAll(component.getIncludedServiceCodes());
        }
        return Collections.unmodifiableSet(codes);
    }

    private boolean containsHierarchyReference(ServiceComponent candidate, String targetPackageCode) {
        if (candidate.getCode().equalsIgnoreCase(targetPackageCode)) {
            return true;
        }
        if (candidate instanceof ServicePackage childPackage) {
            for (ServiceComponent nested : childPackage.getComponents()) {
                if (containsHierarchyReference(nested, targetPackageCode)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServicePackage that)) return false;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
