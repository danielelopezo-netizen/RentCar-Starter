package edu.rentcar.repository;

import edu.rentcar.data.SampleData;
import edu.rentcar.model.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory repository for managing Customer records during application execution.
 */
public class CustomerRepository {
    private final List<Customer> customers = new ArrayList<>(SampleData.customers());

    public List<Customer> findAll() {
        return List.copyOf(customers);
    }

    public Optional<Customer> findById(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return Optional.empty();
        }
        String trimmed = customerId.trim();
        return customers.stream()
                .filter(c -> c.getId().equalsIgnoreCase(trimmed))
                .findFirst();
    }

    public int count() {
        return customers.size();
    }
}
