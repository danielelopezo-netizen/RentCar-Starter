package edu.rentcar.adapter;

import com.rentcar.paymentclient.PaymentApiRequest;
import com.rentcar.paymentclient.PaymentApiResponse;
import edu.rentcar.model.Customer;
import edu.rentcar.model.PaymentRequest;
import edu.rentcar.model.PaymentResult;
import edu.rentcar.model.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RF-004 (Adapter Pattern: PaymentAdapter).
 * Uses test doubles to isolate the external HTTP payment service.
 */
class PaymentAdapterTest {

    private Customer customer;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        customer = new Customer("CUS-1001", "Sofia Martinez", "sofia@example.com", true);
        paymentRequest = new PaymentRequest(
                "RES-1001",
                customer,
                new BigDecimal("350000.00"),
                "tok_approved"
        );
    }

    @Test
    void processPaymentShouldAdaptApprovedResponseCorrectly() {
        AtomicReference<PaymentApiRequest> capturedRequest = new AtomicReference<>();
        PaymentAdapter adapter = new PaymentAdapter(req -> {
            capturedRequest.set(req);
            return new PaymentApiResponse(200, "APPROVED");
        });

        PaymentResult result = adapter.processPayment(paymentRequest);

        assertNotNull(capturedRequest.get());
        assertEquals("RES-1001", capturedRequest.get().reservationId());
        assertEquals("CUS-1001", capturedRequest.get().customerId());
        assertEquals(350000.0, capturedRequest.get().amount());
        assertEquals("tok_approved", capturedRequest.get().paymentToken());

        assertTrue(result.isApproved());
        assertEquals(PaymentStatus.APPROVED, result.getStatus());
        assertEquals(200, result.getHttpStatusCode());
    }

    @Test
    void processPaymentShouldAdaptDeclinedResponseCorrectly() {
        PaymentAdapter adapter = new PaymentAdapter(req -> new PaymentApiResponse(200, "NOT_APPROVED"));

        PaymentResult result = adapter.processPayment(paymentRequest);

        assertFalse(result.isApproved());
        assertTrue(result.isDeclined());
        assertEquals(PaymentStatus.DECLINED, result.getStatus());
        assertEquals(200, result.getHttpStatusCode());
    }

    @Test
    void processPaymentShouldHandleHttp503TechnicalErrorGracefully() {
        PaymentAdapter adapter = new PaymentAdapter(req -> new PaymentApiResponse(503, "PAYMENT_SERVICE_UNAVAILABLE"));

        PaymentResult result = adapter.processPayment(paymentRequest);

        assertFalse(result.isApproved());
        assertEquals(PaymentStatus.TECHNICAL_ERROR, result.getStatus());
        assertEquals(503, result.getHttpStatusCode());
    }

    @Test
    void processPaymentShouldHandleHttp400InvalidRequestError() {
        PaymentAdapter adapter = new PaymentAdapter(req -> new PaymentApiResponse(400, "INVALID_REQUEST"));

        PaymentResult result = adapter.processPayment(paymentRequest);

        assertFalse(result.isApproved());
        assertEquals(PaymentStatus.TECHNICAL_ERROR, result.getStatus());
        assertEquals(400, result.getHttpStatusCode());
    }

    @Test
    void processPaymentShouldHandleConnectionFailureIOException() {
        PaymentAdapter adapter = new PaymentAdapter(req -> {
            throw new IOException("Connection refused");
        });

        PaymentResult result = adapter.processPayment(paymentRequest);

        assertFalse(result.isApproved());
        assertEquals(PaymentStatus.CONNECTION_ERROR, result.getStatus());
    }
}
