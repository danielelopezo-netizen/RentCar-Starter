package edu.rentcar.model;

import java.util.Objects;

/**
 * Encapsulates the outcome of a complete rental workflow execution coordinated by RentalFacade (RF-002).
 */
public final class RentalOperationResult {
    private final boolean success;
    private final Reservation reservation;
    private final PricingBreakdown pricingBreakdown;
    private final PaymentResult paymentResult;
    private final String userMessage;

    public RentalOperationResult(boolean success,
                                 Reservation reservation,
                                 PricingBreakdown pricingBreakdown,
                                 PaymentResult paymentResult,
                                 String userMessage) {
        this.success = success;
        this.reservation = reservation;
        this.pricingBreakdown = pricingBreakdown;
        this.paymentResult = paymentResult;
        this.userMessage = Objects.requireNonNullElse(userMessage, "");
    }

    public boolean isSuccess() {
        return success;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public PricingBreakdown getPricingBreakdown() {
        return pricingBreakdown;
    }

    public PaymentResult getPaymentResult() {
        return paymentResult;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
