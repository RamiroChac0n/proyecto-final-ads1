package com.mycompany.service;

import com.mycompany.model.entity.Customer;
import jakarta.ejb.Local;
import java.util.List;

/**
 * Service interface for Customer operations
 * @author ramir
 */
@Local
public interface ICustomerService {

    /**
     * Save a new customer
     * @param customer Customer to save
     * @return Saved customer
     * @throws IllegalArgumentException if tax ID already exists
     */
    Customer save(Customer customer);

    /**
     * Update an existing customer
     * @param customer Customer to update
     * @return Updated customer
     * @throws IllegalArgumentException if tax ID is duplicated
     */
    Customer edit(Customer customer);

    /**
     * Delete a customer (soft delete - sets isActive to false)
     * @param customer Customer to delete
     */
    void delete(Customer customer);

    /**
     * Get all active customers
     * @return List of active customers
     */
    List<Customer> list();

    /**
     * Find customer by ID
     * @param id Customer ID
     * @return Customer if found, null otherwise
     */
    Customer findById(Integer id);

    /**
     * Find customer by tax ID (NIT)
     * @param taxId Tax ID to search for
     * @return Customer if found, null otherwise
     */
    Customer findByTaxId(String taxId);

    /**
     * Find customer by phone number
     * @param phone Phone number to search for
     * @return Customer if found, null otherwise
     */
    Customer findByPhone(String phone);
}
