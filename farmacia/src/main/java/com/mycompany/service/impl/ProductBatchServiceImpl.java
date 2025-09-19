package com.mycompany.service.impl;

import com.mycompany.model.entity.InventoryMovement;
import com.mycompany.model.entity.Product;
import com.mycompany.model.entity.ProductBatch;
import com.mycompany.model.entity.enums.MovementType;
import com.mycompany.repository.ProductBatchRepository;
import com.mycompany.service.IProductBatchService;
import com.mycompany.service.IProductService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.util.Date;
import java.util.List;

/**
 * Implementation of ProductBatch business operations
 * @author ramir
 */
@Stateless
public class ProductBatchServiceImpl implements IProductBatchService {

    @EJB
    private ProductBatchRepository productBatchRepository;

    @EJB
    private IProductService productService;

    @Override
    public ProductBatch addBatchToExistingProduct(Long productId, ProductBatch batch) {
        // Validate that the product exists
        Product product = productService.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product with ID " + productId + " does not exist");
        }

        // Set the product in the batch
        batch.setProduct(product);

        // Check if batch number already exists for this product
        if (productBatchRepository.existsByProductAndBatchNumber(product, batch.getBatchNumber())) {
            throw new IllegalArgumentException("Batch number '" + batch.getBatchNumber() +
                                             "' already exists for product " + product.getCommercialName());
        }

        // Validate business rules
        validateBatch(batch);

        // Save the batch
        ProductBatch savedBatch = productBatchRepository.save(batch);

        // Create inventory movement record
        createInventoryMovement(savedBatch, MovementType.IN, savedBatch.getQuantityReceived(),
                              "Initial batch reception");

        return savedBatch;
    }

    @Override
    public ProductBatch save(ProductBatch batch) {
        validateBatch(batch);
        return productBatchRepository.save(batch);
    }

    @Override
    public ProductBatch edit(ProductBatch batch) {
        validateBatch(batch);
        return productBatchRepository.update(batch);
    }

    @Override
    public void delete(ProductBatch batch) {
        productBatchRepository.delete(batch);
    }

    @Override
    public List<ProductBatch> list() {
        return productBatchRepository.findAll();
    }

    @Override
    public ProductBatch findById(Integer batchId) {
        return productBatchRepository.findById(batchId);
    }

    @Override
    public List<ProductBatch> findByProduct(Product product) {
        return productBatchRepository.findByProduct(product);
    }

    @Override
    public List<ProductBatch> findAvailableBatches() {
        return productBatchRepository.findAvailableBatches();
    }

    @Override
    public List<ProductBatch> findExpiringBatches(Date date) {
        return productBatchRepository.findByExpirationDateBefore(date);
    }

    @Override
    public boolean batchNumberExists(Product product, String batchNumber) {
        return productBatchRepository.existsByProductAndBatchNumber(product, batchNumber);
    }

    @Override
    public ProductBatch updateBatchQuantity(Integer batchId, Integer quantityChange) {
        ProductBatch batch = findById(batchId);
        if (batch == null) {
            throw new IllegalArgumentException("Batch with ID " + batchId + " does not exist");
        }

        int newQuantity = batch.getQuantityAvailable() + quantityChange;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Insufficient quantity in batch. Available: " +
                                             batch.getQuantityAvailable() + ", Requested: " + Math.abs(quantityChange));
        }

        batch.setQuantityAvailable(newQuantity);
        ProductBatch updatedBatch = productBatchRepository.update(batch);

        // Create inventory movement record
        MovementType movementType = quantityChange > 0 ? MovementType.IN : MovementType.OUT;
        createInventoryMovement(updatedBatch, movementType, Math.abs(quantityChange),
                              quantityChange > 0 ? "Stock adjustment (increase)" : "Stock reduction");

        return updatedBatch;
    }

    /**
     * Validate batch business rules
     * @param batch The batch to validate
     */
    private void validateBatch(ProductBatch batch) {
        if (batch.getQuantityReceived() == null || batch.getQuantityReceived() < 0) {
            throw new IllegalArgumentException("Quantity received must be greater than or equal to 0");
        }

        if (batch.getQuantityAvailable() == null || batch.getQuantityAvailable() < 0) {
            throw new IllegalArgumentException("Quantity available must be greater than or equal to 0");
        }

        if (batch.getQuantityAvailable() > batch.getQuantityReceived()) {
            throw new IllegalArgumentException("Quantity available cannot be greater than quantity received");
        }

        if (batch.getUnitCost() == null || batch.getUnitCost().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit cost must be greater than or equal to 0");
        }

        if (batch.getSalePrice() == null || batch.getSalePrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Sale price must be greater than or equal to 0");
        }

        if (batch.getExpirationDate() == null) {
            throw new IllegalArgumentException("Expiration date is required");
        }

        if (batch.getExpirationDate().before(new Date())) {
            throw new IllegalArgumentException("Expiration date cannot be in the past");
        }

        if (batch.getBatchNumber() == null || batch.getBatchNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Batch number is required");
        }
    }

    /**
     * Create an inventory movement record
     * @param batch The batch
     * @param movementType The type of movement
     * @param quantity The quantity moved
     * @param reason The reason for the movement
     */
    private void createInventoryMovement(ProductBatch batch, MovementType movementType,
                                       Integer quantity, String reason) {
        InventoryMovement movement = InventoryMovement.builder()
                .product(batch.getProduct())
                .batch(batch)
                .movementType(movementType)
                .quantity(quantity)
                .reason(reason)
                .build();

        // Note: In a real implementation, you would inject an InventoryMovementService
        // and save this movement. For now, this is just a placeholder to show the pattern.
        // inventoryMovementService.save(movement);
    }
}