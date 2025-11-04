package com.mycompany.controller;

import com.mycompany.model.entity.Product;
import com.mycompany.model.entity.ProductBatch;
import com.mycompany.model.entity.enums.Role;
import com.mycompany.model.entity.User;
import com.mycompany.service.IProductService;
import com.mycompany.service.IProductBatchService;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import lombok.Data;
import org.primefaces.PrimeFaces;

/**
 * JSF Managed Bean controller for displaying product details and managing product batches.
 * <p>
 * This view-scoped controller provides a detailed view of a single product and manages
 * its associated batch inventory. It handles batch CRUD operations, expiration tracking,
 * and stock availability visualization. The controller is accessed via URL parameter
 * (productId) from the product list page.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Display comprehensive product information (pharmaceutical details, stock levels)</li>
 *   <li>Batch CRUD operations (create, read, update, delete)</li>
 *   <li>Batch expiration status tracking with visual indicators</li>
 *   <li>Filter batches by active/inactive status</li>
 *   <li>Validate batch business rules (quantities, dates, pricing)</li>
 *   <li>Calculate stock availability across all batches</li>
 * </ul>
 *
 * <h3>Batch Validation Rules:</h3>
 * <ul>
 *   <li>Batch number is required and must be unique per product</li>
 *   <li>Quantity available cannot exceed quantity received</li>
 *   <li>Unit cost and sale price must be non-negative</li>
 *   <li>Expiration date is required and must be after manufacture date</li>
 * </ul>
 *
 * <h3>Expiration Status Indicators:</h3>
 * <ul>
 *   <li><strong>Expired:</strong> Batch has passed expiration date (red)</li>
 *   <li><strong>Warning:</strong> Expiring within 30 days (orange)</li>
 *   <li><strong>Caution:</strong> Expiring within 31-90 days (blue)</li>
 *   <li><strong>Good:</strong> More than 90 days until expiration (green)</li>
 * </ul>
 *
 * <h3>Access Control:</h3>
 * Authenticated users can view product details. Batch management (create, edit, delete)
 * requires ADMIN role.
 *
 * <h3>JSF Scope:</h3>
 * {@code @ViewScoped} - Data is loaded per page view and discarded on navigation.
 * Prevents stale data when viewing different products.
 *
 * @author ramir
 * @version 1.0
 * @see Product
 * @see ProductBatch
 * @see IProductService
 * @see IProductBatchService
 */
@Data
@Named(value = "productDetailsController")
@ViewScoped
public class ProductDetailsController implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(ProductDetailsController.class.getName());

    @EJB
    private IProductService productService;

    @EJB
    private IProductBatchService productBatchService;

    private Product product;
    private Long productId;

    private ProductBatch currentBatch;
    private List<ProductBatch> batches;
    private boolean showInactiveBatches = false;

    /**
     * Initializes the controller after dependency injection is complete.
     * <p>
     * This method extracts the {@code productId} from the URL request parameters,
     * validates it, and loads the corresponding product and its batches. If the
     * productId is invalid or missing, the user is redirected back to the products
     * list page.
     * </p>
     *
     * <h4>URL Parameter:</h4>
     * <pre>
     * product-details.xhtml?productId={id}
     * </pre>
     *
     * <h4>Validation:</h4>
     * <ul>
     *   <li>Parameter must be present and non-empty</li>
     *   <li>Parameter must be a valid Long integer</li>
     *   <li>Product with given ID must exist in database</li>
     * </ul>
     *
     * @see PostConstruct
     * @see #loadProduct()
     * @see #loadBatches()
     */
    @PostConstruct
    public void init() {
        // LOGGER.info("ProductDetailsController.init() - Starting initialization");

        String productIdParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("productId");

        if (productIdParam != null && !productIdParam.isEmpty()) {
            try {
                productId = Long.valueOf(productIdParam);
                loadProduct();
                loadBatches();
            } catch (NumberFormatException e) {
                LOGGER.severe("Invalid productId parameter: " + productIdParam);
                redirectToProducts();
            }
        } else {
            LOGGER.warning("No productId parameter provided");
            redirectToProducts();
        }

        // LOGGER.info("ProductDetailsController.init() - Initialization completed");
    }

    /**
     * Checks if the currently logged-in user has ADMIN role.
     * <p>
     * This method is used to conditionally render batch management controls
     * (create, edit, delete buttons) in the UI. Only ADMIN users can modify batches.
     * </p>
     *
     * @return {@code true} if current user is logged in and has {@link Role#ADMIN} role,
     *         {@code false} otherwise
     * @see Role#ADMIN
     */
    public boolean isAdmin() {
        User currentUser = getCurrentUser();
        return currentUser != null && Role.ADMIN.equals(currentUser.getRole());
    }

    /**
     * Retrieves the currently authenticated user from the session.
     * <p>
     * This method extracts the User object stored in the HTTP session map during
     * the login process performed by UserController.
     * </p>
     *
     * @return The authenticated {@link User} object, or {@code null} if no user is logged in
     */
    public User getCurrentUser() {
        return (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
    }

    /**
     * Checks whether a user is currently authenticated in the session.
     *
     * @return {@code true} if a user object exists in session, {@code false} otherwise
     */
    public boolean isLoggedIn() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user") != null;
    }

    /**
     * Page guard that redirects unauthenticated users to the login page.
     * <p>
     * This method should be invoked in XHTML using the {@code preRenderView} event
     * to enforce authentication before displaying product details.
     * </p>
     *
     * <h4>Usage in XHTML:</h4>
     * <pre>
     * &lt;f:event type="preRenderView" listener="#{productDetailsController.checkAccess}" /&gt;
     * </pre>
     *
     * @throws IOException if redirect fails (exception is logged)
     */
    public void checkAccess() {
        if (!isLoggedIn()) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
            } catch (Exception e) {
                LOGGER.severe("Error redirecting to login: " + e.getMessage());
            }
        }
    }

    /**
     * Redirects the user to the products list page.
     * <p>
     * This private utility method is called when:
     * <ul>
     *   <li>No productId parameter is provided in the URL</li>
     *   <li>The productId parameter is invalid (not a number)</li>
     *   <li>The product with the given ID is not found in the database</li>
     * </ul>
     * Prevents displaying an error page and provides better user experience.
     * </p>
     *
     * @throws IOException if redirect fails (exception is logged)
     */
    private void redirectToProducts() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("products.xhtml");
        } catch (Exception e) {
            LOGGER.severe("Error redirecting to products: " + e.getMessage());
        }
    }

    /**
     * Loads the product entity from the database using the productId.
     * <p>
     * This method retrieves the complete product information including all
     * pharmaceutical details, stock thresholds, and related reference data.
     * If the product is not found, displays an error message and redirects
     * to the products list page.
     * </p>
     *
     * @see IProductService#findById(Long)
     */
    public void loadProduct() {
        if (productId != null) {
            product = productService.findById(productId);
            if (product == null) {
                LOGGER.warning("Product not found with ID: " + productId);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Producto no encontrado"));
                redirectToProducts();
            }
        }
    }

    /**
     * Loads all batches associated with the current product from the database.
     * <p>
     * This method retrieves product batches filtered by the current user's branch
     * and applies additional filtering based on the {@code showInactiveBatches} flag.
     * By default, only active batches from the user's branch are displayed.
     * </p>
     *
     * <h4>Filtering:</h4>
     * <ul>
     *   <li><strong>Branch filter:</strong> Shows only batches from the user's assigned branch</li>
     *   <li><strong>Active only (default):</strong> Shows batches where isActive = true</li>
     *   <li><strong>All batches:</strong> Shows both active and inactive batches (when toggled)</li>
     * </ul>
     *
     * @see IProductBatchService#findByProductAndBranch(Product, Branch)
     * @see #toggleInactiveBatches()
     */
    public void loadBatches() {
        if (product != null) {
            User currentUser = getCurrentUser();

            // Filter batches by user's branch
            if (currentUser != null && currentUser.getBranch() != null) {
                batches = productBatchService.findByProductAndBranch(product, currentUser.getBranch());
            } else {
                // Fallback: show all batches if user has no branch assigned
                batches = productBatchService.findByProduct(product);
            }

            if (!showInactiveBatches) {
                batches = batches.stream()
                        .filter(batch -> batch.getIsActive())
                        .toList();
            }
            // LOGGER.info("Loaded " + batches.size() + " batches for product: " + product.getCommercialName());
        }
    }

    /**
     * Toggles the visibility of inactive batches and reloads the batch list.
     * <p>
     * This method is called when the user clicks the "Show Inactive Batches" checkbox.
     * It flips the {@code showInactiveBatches} flag and reloads the batches with
     * the new filter applied.
     * </p>
     *
     * @see #loadBatches()
     */
    public void toggleInactiveBatches() {
        loadBatches();
    }

    /**
     * Initializes a new blank ProductBatch object with default values for creation.
     * <p>
     * This method is called when opening the batch creation dialog. The new batch
     * is pre-populated with sensible defaults:
     * <ul>
     *   <li><strong>product:</strong> Current product being viewed</li>
     *   <li><strong>branch:</strong> Current user's branch (for multi-branch inventory segregation)</li>
     *   <li><strong>quantityReceived:</strong> 0 units</li>
     *   <li><strong>quantityAvailable:</strong> 0 units</li>
     *   <li><strong>unitCost:</strong> 0.00</li>
     *   <li><strong>salePrice:</strong> 0.00</li>
     *   <li><strong>expirationDate:</strong> 1 year from now (default shelf life)</li>
     *   <li><strong>receivedDate:</strong> Today's date</li>
     *   <li><strong>isActive:</strong> true</li>
     *   <li><strong>isExpired:</strong> false</li>
     * </ul>
     * </p>
     *
     * @see ProductBatch.ProductBatchBuilder
     */
    public void createNewBatch() {
        // LOGGER.info("ProductDetailsController.createNewBatch() - Creating new batch");

        // Calculate default expiration date (1 year from now)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        Date defaultExpirationDate = calendar.getTime();

        currentBatch = ProductBatch.builder()
                .product(product)
                .branch(getCurrentUser().getBranch())
                .quantityReceived(0)
                .quantityAvailable(0)
                .unitCost(BigDecimal.ZERO)
                .salePrice(BigDecimal.ZERO)
                .expirationDate(defaultExpirationDate)
                .receivedDate(new Date())
                .isActive(true)
                .isExpired(false)
                .build();

        // LOGGER.info("ProductDetailsController.createNewBatch() - New batch created");
    }

    /**
     * Prepares an existing batch for editing by setting it as the current batch.
     * <p>
     * This method is called when the user clicks the edit button on a batch in the
     * table. It loads the selected batch into the form dialog for modification.
     * All batch fields can be edited except the product association.
     * </p>
     *
     * @param batch The {@link ProductBatch} to edit (must not be {@code null})
     */
    public void editBatch(ProductBatch batch) {
        // LOGGER.info("ProductDetailsController.editBatch() - Editing batch: " + batch.getBatchNumber());
        currentBatch = batch;
    }

    /**
     * Saves a new batch or updates an existing batch with automatic inventory tracking.
     * <p>
     * This method performs comprehensive validation before persisting the batch.
     * It handles both create and update operations based on the presence of batchId:
     * <ul>
     *   <li>If batchId is {@code null}, creates a new batch via service layer</li>
     *   <li>If batchId exists, updates the existing batch</li>
     * </ul>
     * </p>
     *
     * <h4>Validation Performed:</h4>
     * <ul>
     *   <li>Batch number is required and non-empty</li>
     *   <li>Quantity available ≤ quantity received</li>
     *   <li>Unit cost and sale price ≥ 0</li>
     *   <li>Expiration date is required</li>
     *   <li>Expiration date &gt; manufacture date (if manufacture date provided)</li>
     * </ul>
     *
     * <h4>Inventory Movement Tracking:</h4>
     * <p>
     * Both create and update operations automatically generate inventory movements:
     * </p>
     * <ul>
     *   <li><strong>New Batch:</strong> Creates ADJUSTMENT movement with quantityReceived</li>
     *   <li><strong>Edit Batch:</strong> Creates ADJUSTMENT movement if quantity changed (positive/negative)</li>
     *   <li><strong>User Attribution:</strong> Movements track the current logged-in user</li>
     *   <li><strong>Kardex Display:</strong> Movements appear in product-kardex.xhtml</li>
     * </ul>
     *
     * <h4>UI Interaction:</h4>
     * After successful save, the batch list is refreshed, the dialog is closed,
     * and UI components are updated via AJAX. Validation errors are displayed
     * with detailed messages.
     *
     * @throws IllegalArgumentException if validation fails (caught and displayed to user)
     * @see #validateBatch()
     * @see #getCurrentUser()
     * @see IProductBatchService#addBatchToExistingProduct(Long, ProductBatch, User)
     * @see IProductBatchService#edit(ProductBatch, User)
     */
    public void saveBatch() {
        try {
            // LOGGER.info("ProductDetailsController.saveBatch() - Saving batch: " + currentBatch.getBatchNumber());

            validateBatch();

            // Get current user from session
            User currentUser = getCurrentUser();

            // Ensure branch is set for new batches (safety check)
            if (currentBatch.getBranch() == null && currentUser.getBranch() != null) {
                currentBatch.setBranch(currentUser.getBranch());
            }

            if (currentBatch.getBatchId() == null) {
                // New batch
                productBatchService.addBatchToExistingProduct(productId, currentBatch, currentUser);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Lote agregado exitosamente"));
            } else {
                // Existing batch
                productBatchService.edit(currentBatch, currentUser);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Lote actualizado exitosamente"));
            }

            loadBatches();
            PrimeFaces.current().executeScript("PF('dlgBatch').hide()");
            PrimeFaces.current().ajax().update("form:messages", "form:dt-batches");

        } catch (IllegalArgumentException e) {
            LOGGER.warning("Validation error saving batch: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Validación", e.getMessage()));
        } catch (Exception e) {
            LOGGER.severe("Error saving batch: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al guardar el lote: " + e.getMessage()));
        }
    }

    /**
     * Deletes a batch from the database.
     * <p>
     * This method removes the selected batch from inventory. After successful deletion,
     * the batch list is refreshed, the delete dialog is closed, and UI components are
     * updated via AJAX. A success message is displayed to the user.
     * </p>
     *
     * <p>
     * <strong>Warning:</strong> Deletion may fail if the batch has been referenced in
     * sales transactions or inventory movements due to foreign key constraints. In such
     * cases, consider marking the batch as inactive instead of deleting it.
     * </p>
     *
     * @see IProductBatchService#delete(ProductBatch)
     */
    public void deleteBatch() {
        try {
            // LOGGER.info("ProductDetailsController.deleteBatch() - Deleting batch: " + currentBatch.getBatchNumber());

            productBatchService.delete(currentBatch);
            loadBatches();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Lote eliminado exitosamente"));

        } catch (Exception e) {
            LOGGER.severe("Error deleting batch: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al eliminar el lote: " + e.getMessage()));
        }

        PrimeFaces.current().executeScript("PF('dlgDeleteBatch').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:dt-batches");
    }

    /**
     * Validates batch business rules before saving to database.
     * <p>
     * This private method enforces all business constraints for product batches.
     * It is called by {@link #saveBatch()} before persistence operations.
     * Validation failures throw {@link IllegalArgumentException} with descriptive
     * error messages that are displayed to the user.
     * </p>
     *
     * <h4>Validation Rules:</h4>
     * <ul>
     *   <li><strong>Batch Number:</strong> Required, non-null, non-empty</li>
     *   <li><strong>Quantity Received:</strong> Must be ≥ 0</li>
     *   <li><strong>Quantity Available:</strong> Must be ≥ 0 and ≤ quantity received</li>
     *   <li><strong>Unit Cost:</strong> Must be ≥ 0</li>
     *   <li><strong>Sale Price:</strong> Must be ≥ 0</li>
     *   <li><strong>Expiration Date:</strong> Required, must be after manufacture date if provided</li>
     * </ul>
     *
     * @throws IllegalArgumentException if any validation rule is violated
     */
    private void validateBatch() {
        if (currentBatch.getBatchNumber() == null || currentBatch.getBatchNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Batch number is required");
        }

        if (currentBatch.getQuantityReceived() == null || currentBatch.getQuantityReceived() < 0) {
            throw new IllegalArgumentException("Quantity received must be greater than or equal to 0");
        }

        if (currentBatch.getQuantityAvailable() == null || currentBatch.getQuantityAvailable() < 0) {
            throw new IllegalArgumentException("Quantity available must be greater than or equal to 0");
        }

        if (currentBatch.getQuantityAvailable() > currentBatch.getQuantityReceived()) {
            throw new IllegalArgumentException("Quantity available cannot be greater than quantity received");
        }

        if (currentBatch.getUnitCost() == null || currentBatch.getUnitCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit cost must be greater than or equal to 0");
        }

        if (currentBatch.getSalePrice() == null || currentBatch.getSalePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Sale price must be greater than or equal to 0");
        }

        if (currentBatch.getExpirationDate() == null) {
            throw new IllegalArgumentException("Expiration date is required");
        }

        if (currentBatch.getManufactureDate() != null &&
            currentBatch.getExpirationDate().before(currentBatch.getManufactureDate())) {
            throw new IllegalArgumentException("Expiration date must be after manufacture date");
        }
    }

    /**
     * Determines the expiration status category for a batch based on days until expiration.
     * <p>
     * This method categorizes batches into four expiration status levels used for
     * visual indicators in the UI (color coding, icons, warnings). The status is
     * calculated based on the batch's {@code daysUntilExpiration} property.
     * </p>
     *
     * <h4>Status Categories:</h4>
     * <ul>
     *   <li><strong>"expired":</strong> Batch is already expired (isExpired = true)</li>
     *   <li><strong>"warning":</strong> Expiring within 30 days (critical - requires immediate action)</li>
     *   <li><strong>"caution":</strong> Expiring within 31-90 days (attention needed)</li>
     *   <li><strong>"good":</strong> More than 90 days until expiration (healthy stock)</li>
     * </ul>
     *
     * @param batch The {@link ProductBatch} to evaluate (must not be {@code null})
     * @return Status string: "expired", "warning", "caution", or "good"
     * @see #getExpirationStatusIcon(ProductBatch)
     * @see #getExpirationStatusClass(ProductBatch)
     */
    public String getExpirationStatus(ProductBatch batch) {
        if (batch.getIsExpired()) {
            return "expired";
        }

        if (batch.getDaysUntilExpiration() != null) {
            if (batch.getDaysUntilExpiration() <= 30) {
                return "warning";
            } else if (batch.getDaysUntilExpiration() <= 90) {
                return "caution";
            }
        }

        return "good";
    }

    /**
     * Returns the PrimeIcons CSS class for the batch expiration status indicator.
     * <p>
     * This UI helper method maps expiration status categories to corresponding
     * PrimeIcons icon classes for visual representation in the batch table.
     * The icons provide quick visual feedback about batch expiration urgency.
     * </p>
     *
     * <h4>Icon Mapping:</h4>
     * <ul>
     *   <li><strong>"expired":</strong> pi-times-circle (X in circle - red)</li>
     *   <li><strong>"warning":</strong> pi-exclamation-triangle (Warning triangle - orange)</li>
     *   <li><strong>"caution":</strong> pi-info-circle (Info circle - blue)</li>
     *   <li><strong>"good":</strong> pi-check-circle (Check in circle - green)</li>
     * </ul>
     *
     * @param batch The {@link ProductBatch} to get icon for (must not be {@code null})
     * @return PrimeIcons CSS class string (e.g., "pi pi-check-circle")
     * @see #getExpirationStatus(ProductBatch)
     * @see #getExpirationStatusClass(ProductBatch)
     */
    public String getExpirationStatusIcon(ProductBatch batch) {
        String status = getExpirationStatus(batch);
        switch (status) {
            case "expired":
                return "pi pi-times-circle";
            case "warning":
                return "pi pi-exclamation-triangle";
            case "caution":
                return "pi pi-info-circle";
            default:
                return "pi pi-check-circle";
        }
    }

    /**
     * Returns the Bootstrap/PrimeFaces text color CSS class for batch expiration status.
     * <p>
     * This UI helper method maps expiration status categories to corresponding
     * text color classes for styling text elements in the batch table. Works in
     * conjunction with {@link #getExpirationStatusIcon(ProductBatch)} to provide
     * consistent color-coded visual feedback.
     * </p>
     *
     * <h4>Color Class Mapping:</h4>
     * <ul>
     *   <li><strong>"expired":</strong> text-danger (red - critical alert)</li>
     *   <li><strong>"warning":</strong> text-warning (orange - urgent attention)</li>
     *   <li><strong>"caution":</strong> text-info (blue - informational)</li>
     *   <li><strong>"good":</strong> text-success (green - healthy)</li>
     * </ul>
     *
     * <h4>Usage in XHTML:</h4>
     * <pre>
     * &lt;span class="#{productDetailsController.getExpirationStatusClass(batch)}"&gt;
     *   #{batch.daysUntilExpiration} days
     * &lt;/span&gt;
     * </pre>
     *
     * @param batch The {@link ProductBatch} to get color class for (must not be {@code null})
     * @return Bootstrap text color CSS class (e.g., "text-success")
     * @see #getExpirationStatus(ProductBatch)
     * @see #getExpirationStatusIcon(ProductBatch)
     */
    public String getExpirationStatusClass(ProductBatch batch) {
        String status = getExpirationStatus(batch);
        switch (status) {
            case "expired":
                return "text-danger";
            case "warning":
                return "text-warning";
            case "caution":
                return "text-info";
            default:
                return "text-success";
        }
    }

    /**
     * Calculates the total available stock quantity across all active batches.
     * <p>
     * This method sums the {@code quantityAvailable} field from all batches
     * currently loaded in the controller (respecting the active/inactive filter).
     * Used for displaying inventory summary statistics in the UI.
     * </p>
     *
     * <h4>Calculation:</h4>
     * <ul>
     *   <li>Iterates through all batches in the current view</li>
     *   <li>Sums quantityAvailable (treating null values as 0)</li>
     *   <li>Returns total available units across all batches</li>
     * </ul>
     *
     * @return Total available quantity across all batches (0 if no batches exist)
     * @see ProductBatch#getQuantityAvailable()
     */
    public Integer getTotalAvailableQuantity() {
        if (batches == null || batches.isEmpty()) {
            return 0;
        }
        return batches.stream()
                .mapToInt(batch -> batch.getQuantityAvailable() != null ? batch.getQuantityAvailable() : 0)
                .sum();
    }

    /**
     * Counts the number of batches expiring within 30 days.
     * <p>
     * This method identifies batches in the warning expiration status (≤ 30 days
     * until expiration) that are not yet expired. Used for displaying inventory
     * alert statistics in the UI to help prioritize batch usage.
     * </p>
     *
     * <h4>Criteria:</h4>
     * <ul>
     *   <li>daysUntilExpiration is not null</li>
     *   <li>daysUntilExpiration ≤ 30</li>
     *   <li>isExpired is false (not already expired)</li>
     * </ul>
     *
     * @return Count of batches expiring within 30 days (0 if no batches exist)
     * @see ProductBatch#getDaysUntilExpiration()
     * @see ProductBatch#getIsExpired()
     */
    public Integer getExpiringBatchesCount() {
        if (batches == null || batches.isEmpty()) {
            return 0;
        }
        return (int) batches.stream()
                .filter(batch -> batch.getDaysUntilExpiration() != null &&
                               batch.getDaysUntilExpiration() <= 30 &&
                               !batch.getIsExpired())
                .count();
    }
}