package edu.rentcar.proxy;

import edu.rentcar.adapter.PaymentGateway;
import edu.rentcar.model.PaymentRequest;
import edu.rentcar.model.PaymentResult;
import edu.rentcar.service.CustomerVerificationException;
import edu.rentcar.service.CustomerVerificationService;

import java.util.Objects;

/**
 * Protection Proxy pattern implementation (RF-006).
 * Intercepts calls to the real {@link PaymentGateway} and enforces prior customer authorization
 * via {@link CustomerVerificationService}.
 * Guarantees that unauthorized customers or verification failures never trigger an external payment call.
 */
public class VerifiedPaymentProxy implements PaymentGateway {
    private final PaymentGateway realPaymentGateway;
    private final CustomerVerificationService verificationService;

    public VerifiedPaymentProxy(PaymentGateway realPaymentGateway,
                                CustomerVerificationService verificationService) {
        this.realPaymentGateway = Objects.requireNonNull(realPaymentGateway, "Real PaymentGateway is required.");
        this.verificationService = Objects.requireNonNull(verificationService, "CustomerVerificationService is required.");
    }

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        Objects.requireNonNull(request, "PaymentRequest cannot be null.");

        try {
            boolean authorized = verificationService.verifyCustomerAuthorization(request.getCustomer());
            if (!authorized) {
                return PaymentResult.unauthorizedCustomer(
                        "Customer " + request.getCustomer().getFullName()
                                + " (" + request.getCustomerId() + ") is not authorized to process rental payments."
                );
            }
        } catch (CustomerVerificationException exception) {
            return PaymentResult.unauthorizedCustomer(
                    "Customer verification failed: " + exception.getMessage()
            );
        } catch (RuntimeException exception) {
            return PaymentResult.unauthorizedCustomer(
                    "Unexpected error during customer authorization check. Payment was blocked for safety."
            );
        }

        return realPaymentGateway.processPayment(request);
    }
}
