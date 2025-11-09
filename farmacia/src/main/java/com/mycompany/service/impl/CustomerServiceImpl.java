package com.mycompany.service.impl;

import com.mycompany.model.entity.Customer;
import com.mycompany.repository.CustomerRepository;
import com.mycompany.service.ICustomerService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;

/**
 * Service implementation for Customer operations
 * @author ramir
 */
@Stateless
public class CustomerServiceImpl implements ICustomerService {

    @EJB
    private CustomerRepository customerRepository;

    @Override
    public Customer save(Customer customer) {
        // Validate tax ID uniqueness
        if (customer.getTaxId() != null &&
            customerRepository.taxIdExists(customer.getTaxId(), null)) {
            throw new IllegalArgumentException("Ya existe un cliente con el NIT: " + customer.getTaxId());
        }

        // Ensure isActive is set
        if (customer.getIsActive() == null) {
            customer.setIsActive(true);
        }

        return customerRepository.save(customer);
    }

    @Override
    public Customer edit(Customer customer) {
        // Validate tax ID uniqueness (excluding current customer)
        if (customer.getTaxId() != null &&
            customerRepository.taxIdExists(customer.getTaxId(), customer.getCustomerId())) {
            throw new IllegalArgumentException("Ya existe otro cliente con el NIT: " + customer.getTaxId());
        }

        return customerRepository.update(customer);
    }

    @Override
    public void delete(Customer customer) {
        // Soft delete - just set isActive to false
        customer.setIsActive(false);
        customerRepository.update(customer);
    }

    @Override
    public List<Customer> list() {
        return customerRepository.findAll().stream()
                .filter(c -> c.getIsActive() != null && c.getIsActive())
                .toList();
    }

    @Override
    public Customer findById(Integer id) {
        return customerRepository.findById(id);
    }

    @Override
    public Customer findByTaxId(String taxId) {
        if (taxId == null || taxId.trim().isEmpty()) {
            return null;
        }
        return customerRepository.findByTaxId(taxId.trim());
    }

    @Override
    public Customer findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        return customerRepository.findByPhone(phone.trim());
    }
}
