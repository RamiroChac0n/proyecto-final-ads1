package com.mycompany.service;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.Product;
import com.mycompany.model.entity.ProductBatch;
import com.mycompany.model.entity.User;
import jakarta.ejb.Local;

import java.util.Date;
import java.util.List;

/**
 * Service interface for ProductBatch business operations
 * @author ramir
 */
@Local
public interface IProductBatchService {

    /**
     * Add a new batch to an existing product
     * @param productId The ID of the existing product
     * @param batch The batch information to add
     * @param currentUser The user performing the operation
     * @return The saved ProductBatch
     * @throws IllegalArgumentException if product doesn't exist or batch number already exists
     */
    ProductBatch addBatchToExistingProduct(Long productId, ProductBatch batch, User currentUser);

    /**
     * Save a product batch
     * @param batch The batch to save
     * @return The saved ProductBatch
     */
    ProductBatch save(ProductBatch batch);

    /**
     * Update a product batch
     * @param batch The batch to update
     * @param currentUser The user performing the operation
     * @return The updated ProductBatch
     */
    ProductBatch edit(ProductBatch batch, User currentUser);

    /**
     * Delete a product batch
     * @param batch The batch to delete
     */
    void delete(ProductBatch batch);

    /**
     * List all product batches
     * @return List of all ProductBatches
     */
    List<ProductBatch> list();

    /**
     * Find a batch by ID
     * @param batchId The batch ID
     * @return ProductBatch if found, null otherwise
     */
    ProductBatch findById(Integer batchId);

    /**
     * Find all batches for a specific product
     * @param product The product
     * @return List of ProductBatches for the product
     */
    List<ProductBatch> findByProduct(Product product);

    /**
     * Find all batches for a specific product in a specific branch
     * @param product The product
     * @param branch The branch
     * @return List of ProductBatches for the product in the branch
     */
    List<ProductBatch> findByProductAndBranch(Product product, Branch branch);

    /**
     * Find available batches (quantity > 0)
     * @return List of available ProductBatches
     */
    List<ProductBatch> findAvailableBatches();

    /**
     * Find batches expiring before a specific date
     * @param date The expiration date threshold
     * @return List of ProductBatches expiring before the date
     */
    List<ProductBatch> findExpiringBatches(Date date);

    /**
     * Check if a batch number already exists for a product
     * @param product The product
     * @param batchNumber The batch number
     * @return true if exists, false otherwise
     */
    boolean batchNumberExists(Product product, String batchNumber);

    /**
     * Update batch quantity after a sale or adjustment
     * @param batchId The batch ID
     * @param quantityChange The quantity change (negative for sales, positive for adjustments)
     * @return The updated ProductBatch
     */
    ProductBatch updateBatchQuantity(Integer batchId, Integer quantityChange);
}