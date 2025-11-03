package com.mycompany.service.impl;

import com.mycompany.model.dto.BatchAllocation;
import com.mycompany.model.entity.*;
import com.mycompany.model.entity.enums.MovementType;
import com.mycompany.model.entity.enums.SaleStatus;
import com.mycompany.repository.ProductBatchRepository;
import com.mycompany.repository.SaleRepository;
import com.mycompany.repository.UserRepository;
import com.mycompany.service.ICashRegisterService;
import com.mycompany.service.IInventoryMovementService;
import com.mycompany.service.IProductBatchService;
import com.mycompany.service.ISaleService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementation of Sale business operations
 * Implements TDD tests for FIFO inventory allocation
 * @author ramir
 */
@Stateless
public class SaleServiceImpl implements ISaleService {

    @EJB
    private ProductBatchRepository productBatchRepository;

    @EJB
    private IProductBatchService productBatchService;

    @EJB
    private SaleRepository saleRepository;

    @EJB
    private UserRepository userRepository;

    @EJB
    private ICashRegisterService cashRegisterService;

    @EJB
    private IInventoryMovementService inventoryMovementService;

    @Override
    public List<BatchAllocation> allocateStock(Product product, Integer quantity) {
        // Get available batches ordered by FIFO (expiration date ASC)
        List<ProductBatch> availableBatches = productBatchRepository.findAvailableBatchesByProductFIFO(product);

        // Calculate total available quantity
        int totalAvailable = availableBatches.stream()
                .mapToInt(ProductBatch::getQuantityAvailable)
                .sum();

        // Validate sufficient stock
        if (totalAvailable < quantity) {
            throw new IllegalArgumentException(
                String.format("Insufficient stock for product %s. Requested: %d, Available: %d",
                    product.getCommercialName(), quantity, totalAvailable));
        }

        // Allocate stock from batches using FIFO
        List<BatchAllocation> allocations = new ArrayList<>();
        int remainingQuantity = quantity;

        for (ProductBatch batch : availableBatches) {
            if (remainingQuantity <= 0) {
                break;
            }

            int quantityFromThisBatch = Math.min(remainingQuantity, batch.getQuantityAvailable());

            BatchAllocation allocation = BatchAllocation.builder()
                    .batchId(batch.getBatchId())
                    .quantity(quantityFromThisBatch)
                    .unitPrice(batch.getSalePrice())
                    .unitCost(batch.getUnitCost())
                    .batchNumber(batch.getBatchNumber())
                    .expirationDate(batch.getExpirationDate())
                    .build();

            allocations.add(allocation);
            remainingQuantity -= quantityFromThisBatch;
        }

        return allocations;
    }

    @Override
    public boolean validateStockAvailability(Product product, Integer quantity) {
        Integer totalAvailable = getTotalAvailableQuantity(product);
        return totalAvailable >= quantity;
    }

    @Override
    public Integer getTotalAvailableQuantity(Product product) {
        List<ProductBatch> availableBatches = productBatchRepository.findAvailableBatchesByProductFIFO(product);
        return availableBatches.stream()
                .mapToInt(ProductBatch::getQuantityAvailable)
                .sum();
    }

    /**
     * Apply a batch allocation by updating the batch quantity and creating inventory movement.
     * <p>
     * This method is called during sale processing to reduce inventory and track the movement.
     * Creates an OUT movement to record the sale in the product kardex.
     * </p>
     *
     * @param allocation The allocation to apply
     * @param sale The sale associated with this allocation
     */
    private void applyBatchAllocation(BatchAllocation allocation, Sale sale) {
        // Get the batch
        ProductBatch batch = productBatchRepository.findById(allocation.getBatchId());
        if (batch == null) {
            throw new IllegalArgumentException("Batch with ID " + allocation.getBatchId() + " does not exist");
        }

        // Update batch quantity (negative value to reduce stock)
        productBatchService.updateBatchQuantity(allocation.getBatchId(), -allocation.getQuantity());

        // Create inventory movement for the sale (OUT movement)
        String reason = "Venta #" + (sale.getSaleNumber() != null ? sale.getSaleNumber() : "N/A");
        InventoryMovement movement = InventoryMovement.builder()
                .product(batch.getProduct())
                .batch(batch)
                .movementType(MovementType.OUT)
                .quantity(-allocation.getQuantity()) // Negative for OUT movements
                .reason(reason)
                .user(sale.getUser())
                .branch(sale.getBranch())
                .build();

        inventoryMovementService.save(movement);
    }

    @Override
    public Sale processSale(Sale sale, List<SaleDetail> details) {
        // Validate sale has details
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Sale must have at least one detail");
        }

        // Validate and allocate stock for all items
        for (SaleDetail detail : details) {
            Product product = detail.getProduct();
            Integer quantity = detail.getQuantity();

            // Validate stock availability
            if (!validateStockAvailability(product, quantity)) {
                throw new IllegalArgumentException(
                    String.format("Insufficient stock for product %s", product.getCommercialName()));
            }

            // Allocate stock using FIFO
            List<BatchAllocation> allocations = allocateStock(product, quantity);

            // Apply allocations to reduce inventory and create OUT movements
            for (BatchAllocation allocation : allocations) {
                applyBatchAllocation(allocation, sale);
            }

            // Set the first batch as the batch for this detail
            // (In a real system, you might want to split details by batch)
            if (!allocations.isEmpty()) {
                ProductBatch batch = productBatchRepository.findById(allocations.get(0).getBatchId());
                detail.setBatch(batch);
            }

            // Link detail to sale
            detail.setSale(sale);
        }

        // Set sale details
        sale.setSaleDetails(details);

        // Set default status if not set
        if (sale.getSaleStatus() == null) {
            sale.setSaleStatus(SaleStatus.COMPLETED);
        }

        // Save sale
        Sale savedSale = saleRepository.save(sale);

        // Update cash register totals
        if (savedSale.getCashRegister() != null) {
            cashRegisterService.updateRegisterAfterSale(
                savedSale.getCashRegister(),
                savedSale.getTotalAmount()
            );
        }

        return savedSale;
    }

    @Override
    public Sale save(Sale sale) {
        return saleRepository.save(sale);
    }

    @Override
    public Sale edit(Sale sale) {
        return saleRepository.update(sale);
    }

    @Override
    public void delete(Sale sale) {
        saleRepository.delete(sale);
    }

    @Override
    public List<Sale> list() {
        // Use findAllWithUser to avoid LazyInitializationException in sales history view
        return saleRepository.findAllWithUser();
    }

    @Override
    public Sale findById(Integer saleId) {
        return saleRepository.findById(saleId);
    }

    @Override
    public Sale findByIdWithDetails(Integer saleId) {
        // Load sale with all relationships to avoid LazyInitializationException in detail dialog
        return saleRepository.findByIdWithDetails(saleId);
    }

    @Override
    public Sale cancelSale(Integer saleId, String cancelledBy, String reason) {
        // Find the sale
        Sale sale = findById(saleId);
        if (sale == null) {
            throw new IllegalArgumentException("Sale with ID " + saleId + " not found");
        }

        // Check if already cancelled
        if (SaleStatus.CANCELLED.equals(sale.getSaleStatus())) {
            throw new IllegalArgumentException("Sale is already cancelled");
        }

        // Find the user who is cancelling
        User cancellingUser = userRepository.findById(cancelledBy);
        if (cancellingUser == null) {
            throw new IllegalArgumentException("User with ID " + cancelledBy + " not found");
        }

        // Reverse inventory for each detail
        if (sale.getSaleDetails() != null) {
            for (SaleDetail detail : sale.getSaleDetails()) {
                ProductBatch batch = detail.getBatch();
                if (batch != null) {
                    // Return stock to batch (positive value to increase)
                    productBatchService.updateBatchQuantity(batch.getBatchId(), detail.getQuantity());
                }
            }
        }

        // Update sale status
        sale.setSaleStatus(SaleStatus.CANCELLED);
        sale.setCancellationReason(reason);
        sale.setCancelledBy(cancellingUser);
        sale.setCancellationDate(new Date());

        // Save updated sale
        return saleRepository.update(sale);
    }
}
