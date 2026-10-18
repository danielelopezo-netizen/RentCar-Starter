package edu.rentcar.service;

/**
 * Checked exception representing a technical failure or data inconsistency during customer verification (RF-006).
 */
public class CustomerVerificationException extends RuntimeException {

    public CustomerVerificationException(String message) {
        super(message);
    }

    public CustomerVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
