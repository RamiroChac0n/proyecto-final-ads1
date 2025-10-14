package com.mycompany.service;

import com.mycompany.model.dto.BatchAllocation;
import com.mycompany.model.entity.Product;
import com.mycompany.model.entity.Sale;
import com.mycompany.model.entity.SaleDetail;
import jakarta.ejb.Local;

import java.util.List;

/**
 * Service interface for Sale business operations
 * @author ramir
 */
@Local
public interface ISaleService {

    /**
     * Process a complete sale with multiple details
     * Updates inventory using FIFO allocation
     * @param sale The sale header information
     * @param details The list of sale details
     * @return The saved Sale with generated ID
     * @throws IllegalArgumentException if insufficient stock or validation fails
     */
    Sale processSale(Sale sale, List<SaleDetail> details);

    /**
     * Allocate stock for a product using FIFO method
     * Returns list of batch allocations ordered by expiration date (earliest first)
     * @param product The product to allocate stock for
     * @param quantity The total quantity needed
     * @return List of BatchAllocation showing how stock is distributed across batches
     * @throws IllegalArgumentException if insufficient total stock available
     */
    List<BatchAllocation> allocateStock(Product product, Integer quantity);

    /**
     * Validate if sufficient stock is available for a product
     * @param product The product to check
     * @param quantity The quantity needed
     * @return true if sufficient stock exists across all batches, false otherwise
     */
    boolean validateStockAvailability(Product product, Integer quantity);

    /**
     * Get total available quantity for a product across all batches
     * @param product The product
     * @return Total quantity available (sum of all active, non-expired batches)
     */
    Integer getTotalAvailableQuantity(Product product);

    /**
     * Save a sale
     * @param sale The sale to save
     * @return The saved Sale
     */
    Sale save(Sale sale);

    /**
     * Update a sale
     * @param sale The sale to update
     * @return The updated Sale
     */
    Sale edit(Sale sale);

    /**
     * Delete a sale
     * @param sale The sale to delete
     */
    void delete(Sale sale);

    /**
     * List all sales
     * @return List of all Sales
     */
    List<Sale> list();

    /**
     * Find a sale by ID
     * @param saleId The sale ID
     * @return Sale if found, null otherwise
     */
    Sale findById(Integer saleId);

    /**
     * Cancel a sale and revert inventory
     * @param saleId The sale ID to cancel
     * @param cancelledBy User ID who cancels
     * @param reason Cancellation reason
     * @return The cancelled Sale
     * @throws IllegalArgumentException if sale not found or already cancelled
     */
    Sale cancelSale(Integer saleId, String cancelledBy, String reason);
}
