package edu.rentcar.adapter;

import edu.rentcar.model.PaymentRequest;
import edu.rentcar.model.PaymentResult;

/**
 * Target interface in the Adapter and Subject interface in the Proxy pattern (RF-004, RF-006).
 * Decouples the domain logic from the external HTTP payment API client and its DTOs.
 */
public interface PaymentGateway {

    PaymentResult processPayment(PaymentRequest request);
}
