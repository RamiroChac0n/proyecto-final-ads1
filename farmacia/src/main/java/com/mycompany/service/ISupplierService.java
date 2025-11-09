package com.mycompany.service;

import com.mycompany.model.entity.Supplier;
import jakarta.ejb.Local;
import java.util.List;

/**
 * Service interface for Supplier business logic.
 * Defines the contract for supplier management operations.
 */
@Local
public interface ISupplierService {

    /**
     * Save a new supplier.
     *
     * @param supplier the supplier to save
     * @return the saved supplier
     */
    Supplier save(Supplier supplier);

    /**
     * Update an existing supplier.
     *
     * @param supplier the supplier to update
     * @return the updated supplier
     */
    Supplier edit(Supplier supplier);

    /**
     * Delete a supplier.
     *
     * @param supplier the supplier to delete
     */
    void delete(Supplier supplier);

    /**
     * Get all suppliers.
     *
     * @return list of all suppliers
     */
    List<Supplier> list();

    /**
     * Find a supplier by ID.
     *
     * @param supplierId the supplier ID
     * @return the supplier if found, null otherwise
     */
    Supplier findById(Integer supplierId);

    /**
     * Find a supplier by supplier code.
     *
     * @param supplierCode the supplier code
     * @return the supplier if found, null otherwise
     */
    Supplier findBySupplierCode(String supplierCode);

    /**
     * Find a supplier by tax ID (NIT).
     *
     * @param taxId the tax ID
     * @return the supplier if found, null otherwise
     */
    Supplier findByTaxId(String taxId);
}
