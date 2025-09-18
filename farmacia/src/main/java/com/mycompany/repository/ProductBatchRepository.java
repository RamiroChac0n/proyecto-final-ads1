package com.mycompany.repository;

import com.mycompany.model.entity.Product;
import com.mycompany.model.entity.ProductBatch;
import com.mycompany.repository.persistence.Repository;
import jakarta.ejb.Local;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for ProductBatch entity operations
 * @author ramir
 */
@Local
public interface ProductBatchRepository extends Repository<ProductBatch> {

    /**
     * Find all batches for a specific product
     * @param product The product to search batches for
     * @return List of product batches
     */
    List<ProductBatch> findByProduct(Product product);

    /**
     * Find a specific batch by product and batch number
     * @param product The product
     * @param batchNumber The batch number
     * @return ProductBatch if found, null otherwise
     */
    ProductBatch findByProductAndBatchNumber(Product product, String batchNumber);

    /**
     * Find all available batches (quantity > 0)
     * @return List of available product batches
     */
    List<ProductBatch> findAvailableBatches();

    /**
     * Find batches that expire before the given date
     * @param date The expiration date threshold
     * @return List of batches expiring before the date
     */
    List<ProductBatch> findByExpirationDateBefore(LocalDate date);

    /**
     * Find active batches for a specific product
     * @param product The product
     * @return List of active product batches
     */
    List<ProductBatch> findActiveByProduct(Product product);

    /**
     * Check if a batch number already exists for a product
     * @param product The product
     * @param batchNumber The batch number to check
     * @return true if batch number exists, false otherwise
     */
    boolean existsByProductAndBatchNumber(Product product, String batchNumber);
}