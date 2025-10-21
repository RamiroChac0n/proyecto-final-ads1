package com.mycompany.service;

import com.mycompany.model.entity.Supplier;
import com.mycompany.model.entity.SupplierContact;
import jakarta.ejb.Local;
import java.util.List;

/**
 * Service interface for SupplierContact business logic.
 * Defines the contract for supplier contact management operations.
 */
@Local
public interface ISupplierContactService {

    /**
     * Save a new supplier contact.
     *
     * @param contact the contact to save
     * @return the saved contact
     */
    SupplierContact save(SupplierContact contact);

    /**
     * Update an existing supplier contact.
     *
     * @param contact the contact to update
     * @return the updated contact
     */
    SupplierContact edit(SupplierContact contact);

    /**
     * Delete a supplier contact.
     *
     * @param contact the contact to delete
     */
    void delete(SupplierContact contact);

    /**
     * Get all supplier contacts.
     *
     * @return list of all contacts
     */
    List<SupplierContact> list();

    /**
     * Find a contact by ID.
     *
     * @param contactId the contact ID
     * @return the contact if found, null otherwise
     */
    SupplierContact findById(Integer contactId);

    /**
     * Find all contacts for a specific supplier.
     *
     * @param supplier the supplier
     * @return list of contacts for the supplier
     */
    List<SupplierContact> findBySupplier(Supplier supplier);
}
