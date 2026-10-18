package edu.rentcar.factory;

import java.math.BigDecimal;

/**
 * Abstract Product C in the Abstract Factory pattern (RF-003).
 * Defines the roadside assistance and customer support policy for a rental modality.
 */
public interface AssistancePolicy {

    String getAssistanceName();

    String getAssistanceDescription();

    BigDecimal getDailyAssistanceFee();

    BigDecimal calculateAssistanceCost(long rentalDays);
}
