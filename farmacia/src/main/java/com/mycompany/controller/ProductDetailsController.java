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
 * Controller for Product Details and Batch management
 * @author ramir
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

    // Product details
    private Product product;
    private Long productId;

    // Batch management
    private ProductBatch currentBatch;
    private List<ProductBatch> batches;
    private boolean showInactiveBatches = false;

    @PostConstruct
    public void init() {
        LOGGER.info("ProductDetailsController.init() - Starting initialization");

        // Get productId from request parameter
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

        LOGGER.info("ProductDetailsController.init() - Initialization completed");
    }

    // Check if current user is admin
    public boolean isAdmin() {
        User currentUser = getCurrentUser();
        return currentUser != null && Role.ADMIN.equals(currentUser.getRole());
    }

    // Get current logged-in user
    public User getCurrentUser() {
        return (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user") != null;
    }

    // Page access check
    public void checkAccess() {
        if (!isLoggedIn()) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
            } catch (Exception e) {
                LOGGER.severe("Error redirecting to login: " + e.getMessage());
            }
        }
    }

    private void redirectToProducts() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("products.xhtml");
        } catch (Exception e) {
            LOGGER.severe("Error redirecting to products: " + e.getMessage());
        }
    }

    public void loadProduct() {
        if (productId != null) {
            product = productService.findById(productId);
            if (product == null) {
                LOGGER.warning("Product not found with ID: " + productId);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Product not found"));
                redirectToProducts();
            }
        }
    }

    public void loadBatches() {
        if (product != null) {
            batches = productBatchService.findByProduct(product);
            if (!showInactiveBatches) {
                batches = batches.stream()
                        .filter(batch -> batch.getIsActive())
                        .toList();
            }
            LOGGER.info("Loaded " + batches.size() + " batches for product: " + product.getCommercialName());
        }
    }

    public void toggleInactiveBatches() {
        loadBatches();
    }

    public void createNewBatch() {
        LOGGER.info("ProductDetailsController.createNewBatch() - Creating new batch");

        // Calculate default expiration date (1 year from now)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        Date defaultExpirationDate = calendar.getTime();

        currentBatch = ProductBatch.builder()
                .product(product)
                .quantityReceived(0)
                .quantityAvailable(0)
                .unitCost(BigDecimal.ZERO)
                .salePrice(BigDecimal.ZERO)
                .expirationDate(defaultExpirationDate)
                .receivedDate(new Date())
                .isActive(true)
                .isExpired(false)
                .build();

        LOGGER.info("ProductDetailsController.createNewBatch() - New batch created");
    }

    public void editBatch(ProductBatch batch) {
        LOGGER.info("ProductDetailsController.editBatch() - Editing batch: " + batch.getBatchNumber());
        currentBatch = batch;
    }

    public void saveBatch() {
        try {
            LOGGER.info("ProductDetailsController.saveBatch() - Saving batch: " + currentBatch.getBatchNumber());

            // Validate business rules
            validateBatch();

            if (currentBatch.getBatchId() == null) {
                // New batch
                productBatchService.addBatchToExistingProduct(productId, currentBatch);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Batch added successfully"));
            } else {
                // Existing batch
                productBatchService.edit(currentBatch);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Batch updated successfully"));
            }

            loadBatches();
            PrimeFaces.current().executeScript("PF('dlgBatch').hide()");
            PrimeFaces.current().ajax().update("form:messages", "form:dt-batches");

        } catch (IllegalArgumentException e) {
            LOGGER.warning("Validation error saving batch: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", e.getMessage()));
        } catch (Exception e) {
            LOGGER.severe("Error saving batch: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to save batch: " + e.getMessage()));
        }
    }

    public void deleteBatch() {
        try {
            LOGGER.info("ProductDetailsController.deleteBatch() - Deleting batch: " + currentBatch.getBatchNumber());

            productBatchService.delete(currentBatch);
            loadBatches();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Batch deleted successfully"));

        } catch (Exception e) {
            LOGGER.severe("Error deleting batch: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete batch: " + e.getMessage()));
        }

        PrimeFaces.current().executeScript("PF('dlgDeleteBatch').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:dt-batches");
    }

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
}