package edu.rentcar.service;

import edu.rentcar.model.Customer;
import edu.rentcar.repository.CustomerRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Internal customer authorization verification service (RF-006).
 * Validates whether a customer is authorized to perform rental payments.
 *
 * Behavior:
 * - Verifies that the customer is non-null and has a valid identifier.
 * - If a CustomerRepository is configured, verifies that the customer is registered and matches the stored state.
 * - Returns true only if the customer is explicitly authorized (`customer.isAuthorized() == true`).
 * - Throws {@link CustomerVerificationException} if the customer is null, has a blank ID, or cannot be verified.
 */
public class CustomerVerificationService {
    private final CustomerRepository customerRepository;

    public CustomerVerificationService() {
        this.customerRepository = null;
    }

    public CustomerVerificationService(CustomerRepository customerRepository) {
        this.customerRepository = Objects.requireNonNull(customerRepository, "CustomerRepository cannot be null.");
    }

    /**
     * Verifies whether the given customer is authorized to proceed with a rental payment.
     *
     * @param customer the customer to verify
     * @return true if authorized, false if unauthorized
     * @throws CustomerVerificationException if customer data is invalid or verification fails
     */
    public boolean verifyCustomerAuthorization(Customer customer) {
        if (customer == null) {
            throw new CustomerVerificationException("Customer cannot be null during verification.");
        }
        if (customer.getId() == null || customer.getId().isBlank()) {
            throw new CustomerVerificationException("Customer ID is missing or invalid.");
        }

        if (customerRepository != null) {
            Optional<Customer> storedCustomer = customerRepository.findById(customer.getId());
            if (storedCustomer.isEmpty()) {
                throw new CustomerVerificationException(
                        "Customer '" + customer.getId() + "' is not registered in the system."
                );
            }
            return storedCustomer.get().isAuthorized() && customer.isAuthorized();
        }

        return customer.isAuthorized();
    }
}
