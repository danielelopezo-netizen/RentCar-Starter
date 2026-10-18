package edu.rentcar.model;

import java.util.Objects;

/**
 * Internal domain representation of a payment processing result (RF-004, RF-006).
 */
public final class PaymentResult {
    private final PaymentStatus status;
    private final String message;
    private final int httpStatusCode;

    public PaymentResult(PaymentStatus status, String message, int httpStatusCode) {
        this.status = Objects.requireNonNull(status, "Payment status is required.");
        this.message = message == null ? "" : message;
        this.httpStatusCode = httpStatusCode;
    }

    public static PaymentResult approved(String message) {
        return new PaymentResult(PaymentStatus.APPROVED, message, 200);
    }

    public static PaymentResult declined(String message, int httpStatusCode) {
        return new PaymentResult(PaymentStatus.DECLINED, message, httpStatusCode);
    }

    public static PaymentResult unauthorizedCustomer(String message) {
        return new PaymentResult(PaymentStatus.UNAUTHORIZED_CUSTOMER, message, 0);
    }

    public static PaymentResult technicalError(String message, int httpStatusCode) {
        return new PaymentResult(PaymentStatus.TECHNICAL_ERROR, message, httpStatusCode);
    }

    public static PaymentResult connectionError(String message) {
        return new PaymentResult(PaymentStatus.CONNECTION_ERROR, message, 0);
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public boolean isApproved() {
        return status == PaymentStatus.APPROVED;
    }

    public boolean isDeclined() {
        return status == PaymentStatus.DECLINED;
    }
}
