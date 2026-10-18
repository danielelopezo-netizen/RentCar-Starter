package edu.rentcar.model;

/**
 * Outcomes of a payment operation processed through the payment gateway (RF-004, RF-006).
 */
public enum PaymentStatus {
    APPROVED("Approved"),
    DECLINED("Declined"),
    UNAUTHORIZED_CUSTOMER("Unauthorized Customer"),
    TECHNICAL_ERROR("Technical Error"),
    CONNECTION_ERROR("Connection Error");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
