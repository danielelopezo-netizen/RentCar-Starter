package edu.rentcar.factory;

import edu.rentcar.model.RentalMode;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Registry/Provider for RentalModeFactory implementations (RF-003).
 * Allows registering new rental modality factories without rewriting the main rental workflow.
 */
public final class RentalModeFactoryProvider {
    private static final Map<RentalMode, RentalModeFactory> FACTORIES = new EnumMap<>(RentalMode.class);

    static {
        registerFactory(new BasicRentalModeFactory());
        registerFactory(new StandardRentalModeFactory());
        registerFactory(new PremiumRentalModeFactory());
    }

    private RentalModeFactoryProvider() {
    }

    public static void registerFactory(RentalModeFactory factory) {
        Objects.requireNonNull(factory, "RentalModeFactory cannot be null.");
        FACTORIES.put(factory.getRentalMode(), factory);
    }

    public static RentalModeFactory getFactory(RentalMode mode) {
        Objects.requireNonNull(mode, "RentalMode is required.");
        RentalModeFactory factory = FACTORIES.get(mode);
        if (factory == null) {
            throw new IllegalArgumentException("No factory registered for rental mode: " + mode);
        }
        return factory;
    }
}
