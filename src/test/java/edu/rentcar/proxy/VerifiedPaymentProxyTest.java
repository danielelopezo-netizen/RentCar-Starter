package edu.rentcar.proxy;

import edu.rentcar.adapter.PaymentGateway;
import edu.rentcar.model.Customer;
import edu.rentcar.model.PaymentRequest;
import edu.rentcar.model.PaymentResult;
import edu.rentcar.model.PaymentStatus;
import edu.rentcar.service.CustomerVerificationException;
import edu.rentcar.service.CustomerVerificationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RF-006 (Proxy Pattern: VerifiedPaymentProxy & CustomerVerificationService).
 * Verifies both the authorization result and whether the underlying payment gateway was invoked.
 */
class VerifiedPaymentProxyTest {

    @Test
    void authorizedCustomerShouldInvokeUnderlyingPaymentGateway() {
        AtomicInteger paymentInvocationCount = new AtomicInteger(0);
        PaymentGateway spyGateway = request -> {
            paymentInvocationCount.incrementAndGet();
            return PaymentResult.approved("Approved by spy gateway");
        };

        VerifiedPaymentProxy proxy = new VerifiedPaymentProxy(spyGateway, new CustomerVerificationService());
        Customer authorizedCustomer = new Customer("CUS-1001", "Sofia Martinez", "sofia@example.com", true);
        PaymentRequest request = new PaymentRequest(
                "RES-1001",
                authorizedCustomer,
                new BigDecimal("300000.00"),
                "tok_approved"
        );

        PaymentResult result = proxy.processPayment(request);

        assertTrue(result.isApproved());
        assertEquals(1, paymentInvocationCount.get(), "Underlying payment gateway must be invoked once for authorized customer.");
    }

    @Test
    void unauthorizedCustomerShouldBeBlockedAndNeverInvokePaymentGateway() {
        AtomicInteger paymentInvocationCount = new AtomicInteger(0);
        PaymentGateway spyGateway = request -> {
            paymentInvocationCount.incrementAndGet();
            return PaymentResult.approved("Should never reach here");
        };

        VerifiedPaymentProxy proxy = new VerifiedPaymentProxy(spyGateway, new CustomerVerificationService());
        Customer unauthorizedCustomer = new Customer("CUS-1003", "Valentina Torres", "valentina@example.com", false);
        PaymentRequest request = new PaymentRequest(
                "RES-1002",
                unauthorizedCustomer,
                new BigDecimal("300000.00"),
                "tok_approved"
        );

        PaymentResult result = proxy.processPayment(request);

        assertFalse(result.isApproved());
        assertEquals(PaymentStatus.UNAUTHORIZED_CUSTOMER, result.getStatus());
        assertEquals(0, paymentInvocationCount.get(), "Payment gateway must NEVER be invoked for an unauthorized customer.");
    }

    @Test
    void verificationFailureExceptionShouldBlockPaymentAndNeverInvokeGateway() {
        AtomicInteger paymentInvocationCount = new AtomicInteger(0);
        PaymentGateway spyGateway = request -> {
            paymentInvocationCount.incrementAndGet();
            return PaymentResult.approved("Should never reach here");
        };

        CustomerVerificationService failingVerificationService = new CustomerVerificationService() {
            @Override
            public boolean verifyCustomerAuthorization(Customer customer) {
                throw new CustomerVerificationException("Verification database unreachable");
            }
        };

        VerifiedPaymentProxy proxy = new VerifiedPaymentProxy(spyGateway, failingVerificationService);
        Customer authorizedCustomer = new Customer("CUS-1001", "Sofia Martinez", "sofia@example.com", true);
        PaymentRequest request = new PaymentRequest(
                "RES-1003",
                authorizedCustomer,
                new BigDecimal("300000.00"),
                "tok_approved"
        );

        PaymentResult result = proxy.processPayment(request);

        assertFalse(result.isApproved());
        assertEquals(PaymentStatus.UNAUTHORIZED_CUSTOMER, result.getStatus());
        assertTrue(result.getMessage().contains("Verification database unreachable"));
        assertEquals(0, paymentInvocationCount.get(), "Payment gateway must NEVER be invoked when verification fails.");
    }
}
