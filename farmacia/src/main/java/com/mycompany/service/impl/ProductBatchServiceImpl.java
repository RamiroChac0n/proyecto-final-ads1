package com.mycompany.service.impl;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.InventoryMovement;
import com.mycompany.model.entity.Product;
import com.mycompany.model.entity.ProductBatch;
import com.mycompany.model.entity.User;
import com.mycompany.model.entity.enums.MovementType;
import com.mycompany.repository.ProductBatchRepository;
import com.mycompany.service.IInventoryMovementService;
import com.mycompany.service.IProductBatchService;
import com.mycompany.service.IProductService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.util.Date;
import java.util.List;

/**
 * Implementation of ProductBatch business operations.
 * <p>
 * This stateless EJB service provides comprehensive batch management functionality
 * with automatic inventory movement tracking. All batch creation and modification
 * operations are recorded as inventory movements in the product kardex for
 * complete traceability and audit purposes.
 * </p>
 *
 * <h3>Inventory Movement Tracking:</h3>
 * <ul>
 *   <li><strong>Batch Creation:</strong> Creates ADJUSTMENT movement with positive quantity</li>
 *   <li><strong>Batch Editing:</strong> Creates ADJUSTMENT movement with quantity difference (positive/negative)</li>
 *   <li><strong>User Attribution:</strong> All movements track the user who performed the operation</li>
 * </ul>
 *
 * @author ramir
 * @version 1.1
 * @since 1.0
 * @see IProductBatchService
 * @see ProductBatch
 * @see InventoryMovement
 */
@Stateless
public class ProductBatchServiceImpl implements IProductBatchService {

    @EJB
    private ProductBatchRepository productBatchRepository;

    @EJB
    private IProductService productService;

    @EJB
    private IInventoryMovementService inventoryMovementService;

    /**
     * Adds a new batch to an existing product with automatic inventory movement tracking.
     * <p>
     * This method performs the following operations:
     * <ol>
     *   <li>Validates that the target product exists</li>
     *   <li>Associates the batch with the product</li>
     *   <li>Checks for duplicate batch numbers within the product</li>
     *   <li>Validates batch business rules (quantities, dates, pricing)</li>
     *   <li>Persists the batch to the database</li>
     *   <li>Creates an inventory ADJUSTMENT movement for the reception</li>
     * </ol>
     * </p>
     *
     * <h4>Inventory Movement Details:</h4>
     * <ul>
     *   <li><strong>Type:</strong> ADJUSTMENT</li>
     *   <li><strong>Quantity:</strong> quantityReceived (positive value)</li>
     *   <li><strong>Reason:</strong> "Recepción de lote nuevo - Lote: {batchNumber}"</li>
     *   <li><strong>User:</strong> The user who created the batch</li>
     *   <li><strong>Date:</strong> Automatically set to current timestamp</li>
     * </ul>
     *
     * @param productId The ID of the existing product to add the batch to
     * @param batch The batch information to add (must have valid quantities, dates, and pricing)
     * @param currentUser The user performing the operation (for audit trail)
     * @return The saved ProductBatch with generated ID
     * @throws IllegalArgumentException if product doesn't exist, batch number already exists,
     *         or any validation rule is violated
     * @see #validateBatch(ProductBatch)
     * @see #createInventoryMovement(ProductBatch, MovementType, Integer, String, User)
     */
    @Override
    public ProductBatch addBatchToExistingProduct(Long productId, ProductBatch batch, User currentUser) {
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
        String reason = "Recepción de lote nuevo - Lote: " + savedBatch.getBatchNumber();
        createInventoryMovement(savedBatch, MovementType.ADJUSTMENT, savedBatch.getQuantityReceived(),
                              reason, currentUser);

        return savedBatch;
    }

    @Override
    public ProductBatch save(ProductBatch batch) {
        validateBatch(batch);
        return productBatchRepository.save(batch);
    }

    /**
     * Updates an existing product batch with automatic inventory movement tracking.
     * <p>
     * This method performs the following operations:
     * <ol>
     *   <li>Validates the batch business rules</li>
     *   <li>Retrieves the original batch from database for comparison</li>
     *   <li>Detects changes in quantityAvailable</li>
     *   <li>Persists the updated batch</li>
     *   <li>If quantity changed, creates an inventory ADJUSTMENT movement</li>
     * </ol>
     * </p>
     *
     * <h4>Inventory Movement Tracking:</h4>
     * <p>
     * An inventory movement is created ONLY if the {@code quantityAvailable} field
     * has changed. The movement records the difference between the new and original
     * quantities, which can be positive (increase) or negative (decrease).
     * </p>
     *
     * <h4>Movement Details for Quantity Changes:</h4>
     * <ul>
     *   <li><strong>Type:</strong> ADJUSTMENT</li>
     *   <li><strong>Quantity:</strong> Positive for increases, negative for decreases</li>
     *   <li><strong>Reason (Increase):</strong> "Ajuste de inventario - Incremento de X unidades"</li>
     *   <li><strong>Reason (Decrease):</strong> "Ajuste de inventario - Reducción de X unidades"</li>
     *   <li><strong>User:</strong> The user who modified the batch</li>
     *   <li><strong>Date:</strong> Automatically set to current timestamp</li>
     * </ul>
     *
     * <h4>Example Scenarios:</h4>
     * <pre>
     * Original quantity: 100, New quantity: 150 → Movement: +50 (Incremento)
     * Original quantity: 100, New quantity: 75  → Movement: -25 (Reducción)
     * Original quantity: 100, New quantity: 100 → No movement created
     * </pre>
     *
     * @param batch The batch to update (must have a valid batchId)
     * @param currentUser The user performing the operation (for audit trail)
     * @return The updated ProductBatch
     * @throws IllegalArgumentException if batch doesn't exist or any validation rule is violated
     * @see #validateBatch(ProductBatch)
     * @see #createInventoryMovement(ProductBatch, MovementType, Integer, String, User)
     */
    @Override
    public ProductBatch edit(ProductBatch batch, User currentUser) {
        validateBatch(batch);

        // Get the original batch to compare quantities
        ProductBatch originalBatch = productBatchRepository.findById(batch.getBatchId());
        if (originalBatch == null) {
            throw new IllegalArgumentException("Batch with ID " + batch.getBatchId() + " does not exist");
        }

        // Detect quantity changes
        Integer originalQuantity = originalBatch.getQuantityAvailable();
        Integer newQuantity = batch.getQuantityAvailable();

        // Update the batch
        ProductBatch updatedBatch = productBatchRepository.update(batch);

        // If quantity changed, create inventory movement
        if (!originalQuantity.equals(newQuantity)) {
            Integer quantityDifference = newQuantity - originalQuantity;
            String reason;

            if (quantityDifference > 0) {
                reason = "Ajuste de inventario - Incremento de " + quantityDifference + " unidades";
            } else {
                reason = "Ajuste de inventario - Reducción de " + Math.abs(quantityDifference) + " unidades";
            }

            createInventoryMovement(updatedBatch, MovementType.ADJUSTMENT, quantityDifference,
                                  reason, currentUser);
        }

        return updatedBatch;
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
    public List<ProductBatch> findByProductAndBranch(Product product, Branch branch) {
        return productBatchRepository.findByProductAndBranch(product, branch);
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

        // Note: Inventory movement creation is now handled by the caller
        // - For sales: SaleServiceImpl creates OUT movements
        // - For manual adjustments: Use edit() method which tracks changes

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
     * Creates and persists an inventory movement record for batch operations.
     * <p>
     * This private utility method is responsible for creating inventory movement
     * records that appear in the product kardex. It is called automatically by
     * batch management operations ({@link #addBatchToExistingProduct} and
     * {@link #edit}) to maintain a complete audit trail of inventory changes.
     * </p>
     *
     * <h4>Movement Record Structure:</h4>
     * <ul>
     *   <li><strong>Product:</strong> Extracted from the batch</li>
     *   <li><strong>Batch:</strong> Reference to the specific batch involved</li>
     *   <li><strong>Movement Type:</strong> Typically ADJUSTMENT for batch operations</li>
     *   <li><strong>Quantity:</strong> Can be positive (increase) or negative (decrease)</li>
     *   <li><strong>Reason:</strong> Descriptive text explaining the movement</li>
     *   <li><strong>User:</strong> Who performed the operation (can be null for system operations)</li>
     *   <li><strong>Movement Date:</strong> Auto-set via @PrePersist in InventoryMovement entity</li>
     *   <li><strong>Branch:</strong> Not set (null) for batch operations</li>
     * </ul>
     *
     * <h4>Kardex Display:</h4>
     * <p>
     * The created movement will appear in the product kardex (product-kardex.xhtml):
     * </p>
     * <ul>
     *   <li>Positive quantities → Displayed in ENTRADAS (green)</li>
     *   <li>Negative quantities → Displayed in SALIDAS (red)</li>
     *   <li>Running balance automatically calculated</li>
     * </ul>
     *
     * @param batch The product batch involved in the movement (must not be null)
     * @param movementType The type of inventory movement (typically ADJUSTMENT)
     * @param quantity The quantity change (positive for additions, negative for reductions)
     * @param reason A descriptive reason for the movement (appears in kardex)
     * @param user The user who performed the operation (null for system operations)
     * @see InventoryMovement
     * @see MovementType#ADJUSTMENT
     * @see IInventoryMovementService#save(InventoryMovement)
     */
    private void createInventoryMovement(ProductBatch batch, MovementType movementType,
                                       Integer quantity, String reason, User user) {
        InventoryMovement movement = InventoryMovement.builder()
                .product(batch.getProduct())
                .batch(batch)
                .movementType(movementType)
                .quantity(quantity)
                .reason(reason)
                .user(user)
                .build();

        // Save the inventory movement
        inventoryMovementService.save(movement);
    }
}