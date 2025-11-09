package com.mycompany.service;

import com.mycompany.model.dto.BatchAllocation;
import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.Product;
import com.mycompany.model.entity.Sale;
import com.mycompany.model.entity.SaleDetail;
import com.mycompany.model.entity.enums.SaleStatus;
import jakarta.ejb.Local;

import java.util.Date;
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
     * @param branch The branch to allocate stock from
     * @return List of BatchAllocation showing how stock is distributed across batches
     * @throws IllegalArgumentException if insufficient total stock available
     */
    List<BatchAllocation> allocateStock(Product product, Integer quantity, Branch branch);

    /**
     * Validate if sufficient stock is available for a product
     * @param product The product to check
     * @param quantity The quantity needed
     * @param branch The branch to check stock in
     * @return true if sufficient stock exists across all batches, false otherwise
     */
    boolean validateStockAvailability(Product product, Integer quantity, Branch branch);

    /**
     * Get total available quantity for a product across all batches
     * @param product The product
     * @param branch The branch to check stock in
     * @return Total quantity available (sum of all active, non-expired batches)
     */
    Integer getTotalAvailableQuantity(Product product, Branch branch);

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
     * Find a sale by ID with all details loaded (eager)
     * Loads user, saleDetails, and products to avoid LazyInitializationException
     * @param saleId The sale ID
     * @return Sale with all relationships loaded, or null if not found
     */
    Sale findByIdWithDetails(Integer saleId);

    /**
     * Cancel a sale and revert inventory
     * @param saleId The sale ID to cancel
     * @param cancelledBy User ID who cancels
     * @param reason Cancellation reason
     * @return The cancelled Sale
     * @throws IllegalArgumentException if sale not found or already cancelled
     */
    Sale cancelSale(Integer saleId, String cancelledBy, String reason);

    /**
     * List all sales for a specific branch
     * @param branch The branch to filter by
     * @return List of sales for the given branch
     */
    List<Sale> listByBranch(Branch branch);

    /**
     * Find sales by branch with user information loaded
     * @param branch The branch to filter by
     * @return List of sales with user relationship eagerly loaded
     */
    List<Sale> findByBranchWithUser(Branch branch);

    /**
     * Find sales by branch and date range
     * @param branch The branch to filter by
     * @param fromDate Start date
     * @param toDate End date
     * @return List of sales in the date range for the branch
     */
    List<Sale> findByBranchAndDateRange(Branch branch, Date fromDate, Date toDate);

    /**
     * Find sales by branch and status
     * @param branch The branch to filter by
     * @param status The sale status
     * @return List of sales with the given status for the branch
     */
    List<Sale> findByBranchAndStatus(Branch branch, SaleStatus status);
}
