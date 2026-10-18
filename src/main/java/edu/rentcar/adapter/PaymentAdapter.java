
package edu.rentcar.adapter;

import com.rentcar.paymentclient.PaymentApiClient;
import com.rentcar.paymentclient.PaymentApiRequest;
import com.rentcar.paymentclient.PaymentApiResponse;
import edu.rentcar.model.PaymentRequest;
import edu.rentcar.model.PaymentResult;

import java.io.IOException;
import java.util.Objects;

/**
 * Adapter pattern implementation (RF-004).
 * Adapts the external {@link PaymentApiClient} library and its {@link PaymentApiRequest}/{@link PaymentApiResponse}
 * records to the internal {@link PaymentGateway} abstraction.
 */
public class PaymentAdapter implements PaymentGateway {
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    @FunctionalInterface
    public interface ExternalPaymentCaller {
        PaymentApiResponse call(PaymentApiRequest request) throws IOException, InterruptedException;
    }

    private final ExternalPaymentCaller externalCaller;

    public PaymentAdapter() {
        this(new PaymentApiClient(DEFAULT_BASE_URL));
    }

    public PaymentAdapter(String baseUrl) {
        this(new PaymentApiClient(baseUrl));
    }

    public PaymentAdapter(PaymentApiClient apiClient) {
        Objects.requireNonNull(apiClient, "PaymentApiClient is required.");
        this.externalCaller = apiClient::pay;
    }

    public PaymentAdapter(ExternalPaymentCaller externalCaller) {
        this.externalCaller = Objects.requireNonNull(externalCaller, "ExternalPaymentCaller is required.");
    }

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        Objects.requireNonNull(request, "PaymentRequest cannot be null.");

        PaymentApiRequest apiRequest = new PaymentApiRequest(
                request.getReservationId(),
                request.getCustomerId(),
                request.getAmount().doubleValue(),
                request.getPaymentToken()
        );

        try {
            PaymentApiResponse response = externalCaller.call(apiRequest);
            if (response == null) {
                return PaymentResult.technicalError("Received an empty response from the payment provider.", 502);
            }

            if (response.isApproved()) {
                return PaymentResult.approved("Payment approved by the external payment service.");
            }

            if (response.isDeclined()) {
                return PaymentResult.declined(
                        "Payment was declined by the payment provider (NOT_APPROVED).",
                        response.httpStatus()
                );
            }

            int status = response.httpStatus();
            if (status >= 500) {
                return PaymentResult.technicalError(
                        "External payment service is temporarily unavailable (HTTP " + status + ").",
                        status
                );
            }
            if (status >= 400) {
                return PaymentResult.technicalError(
                        "Payment request was rejected due to an invalid request format (HTTP " + status + ").",
                        status
                );
            }

            return PaymentResult.technicalError(
                    "Unexpected response received from payment provider (HTTP " + status + ").",
                    status
            );
        } catch (IOException exception) {
            return PaymentResult.connectionError(
                    "Could not connect to the external payment service at localhost:8080. Please verify that payment-api.jar is running."
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return PaymentResult.connectionError("Payment request was interrupted before completion.");
        } catch (RuntimeException exception) {
            return PaymentResult.technicalError("Unexpected technical error during payment processing.", 500);
        }
    }
}
