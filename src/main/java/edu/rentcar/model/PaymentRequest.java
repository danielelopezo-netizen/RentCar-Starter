package edu.rentcar.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Internal domain representation of a payment request, decoupled from external API DTOs (RF-004).
 */
public final class PaymentRequest {
    private final String reservationId;
    private final Customer customer;
    private final BigDecimal amount;
    private final String paymentToken;

    public PaymentRequest(String reservationId, Customer customer, BigDecimal amount, String paymentToken) {
        if (reservationId == null || reservationId.isBlank()) {
            throw new IllegalArgumentException("Reservation ID is required for payment.");
        }
        this.reservationId = reservationId.trim();
        this.customer = Objects.requireNonNull(customer, "Customer is required for payment.");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }
        this.amount = amount;
        if (paymentToken == null || paymentToken.isBlank()) {
            throw new IllegalArgumentException("Payment token is required.");
        }
        this.paymentToken = paymentToken.trim();
    }

    public String getReservationId() {
        return reservationId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getCustomerId() {
        return customer.getId();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentToken() {
        return paymentToken;
    }
}
