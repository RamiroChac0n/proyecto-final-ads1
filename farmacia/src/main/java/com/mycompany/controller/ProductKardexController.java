package com.mycompany.controller;

import com.mycompany.model.entity.InventoryMovement;
import com.mycompany.model.entity.Product;
import com.mycompany.model.entity.User;
import com.mycompany.model.entity.enums.MovementType;
import com.mycompany.service.IInventoryMovementService;
import com.mycompany.service.IProductService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import lombok.Data;

/**
 * Controller for Product KARDEX (Inventory Movement History)
 * @author ramir
 */
@Data
@Named(value = "productKardexController")
@ViewScoped
public class ProductKardexController implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(ProductKardexController.class.getName());
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @EJB
    private IProductService productService;

    @EJB
    private IInventoryMovementService inventoryMovementService;

    private Product product;
    private Long productId;
    private List<InventoryMovement> movements;
    private List<KardexRow> kardexRows;

    @PostConstruct
    public void init() {
        LOGGER.info("ProductKardexController.init() - Starting initialization");

        // Get productId from request parameter
        String productIdParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("productId");

        if (productIdParam != null && !productIdParam.isEmpty()) {
            try {
                productId = Long.valueOf(productIdParam);
                loadProduct();
                loadMovements();
                calculateKardex();
            } catch (NumberFormatException e) {
                LOGGER.severe("Invalid productId parameter: " + productIdParam);
                redirectToProducts();
            }
        } else {
            LOGGER.warning("No productId parameter provided");
            redirectToProducts();
        }

        LOGGER.info("ProductKardexController.init() - Initialization completed");
    }

    /**
     * Check if user is logged in
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
     * Check if current user is logged in
     */
    public boolean isLoggedIn() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user") != null;
    }

    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
    }

    private void redirectToProducts() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("products.xhtml");
        } catch (Exception e) {
            LOGGER.severe("Error redirecting to products: " + e.getMessage());
        }
    }

    private void loadProduct() {
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

    private void loadMovements() {
        if (product != null) {
            movements = inventoryMovementService.findByProduct(product);
            LOGGER.info("Loaded " + movements.size() + " movements for product: " + product.getCommercialName());
        }
    }

    /**
     * Calculate KARDEX with running balance
     */
    private void calculateKardex() {
        kardexRows = new ArrayList<>();

        if (movements == null || movements.isEmpty()) {
            return;
        }

        // Reverse the list to calculate from oldest to newest
        List<InventoryMovement> reversedMovements = new ArrayList<>(movements);
        java.util.Collections.reverse(reversedMovements);

        int runningBalance = 0;

        for (InventoryMovement movement : reversedMovements) {
            // Calculate movement effect based on type
            int effectiveQuantity = 0;
            if (movement.getMovementType() == MovementType.IN) {
                effectiveQuantity = movement.getQuantity();
            } else if (movement.getMovementType() == MovementType.OUT) {
                effectiveQuantity = -Math.abs(movement.getQuantity());
            } else { // ADJUSTMENT
                effectiveQuantity = movement.getQuantity();
            }

            runningBalance += effectiveQuantity;

            KardexRow row = new KardexRow();
            row.setMovement(movement);
            row.setEffectiveQuantity(effectiveQuantity);
            row.setRunningBalance(runningBalance);
            kardexRows.add(row);
        }

        // Reverse back to show most recent first
        java.util.Collections.reverse(kardexRows);
    }

    /**
     * Get icon for movement type
     */
    public String getMovementTypeIcon(MovementType type) {
        if (type == null) return "pi pi-question";

        switch (type) {
            case IN:
                return "pi pi-arrow-down";
            case OUT:
                return "pi pi-arrow-up";
            case ADJUSTMENT:
                return "pi pi-sync";
            default:
                return "pi pi-question";
        }
    }

    /**
     * Get CSS class for movement type
     */
    public String getMovementTypeClass(MovementType type) {
        if (type == null) return "";

        switch (type) {
            case IN:
                return "text-success";
            case OUT:
                return "text-danger";
            case ADJUSTMENT:
                return "text-warning";
            default:
                return "";
        }
    }

    /**
     * Get badge severity for movement type
     */
    public String getMovementTypeSeverity(MovementType type) {
        if (type == null) return "info";

        switch (type) {
            case IN:
                return "success";
            case OUT:
                return "danger";
            case ADJUSTMENT:
                return "warning";
            default:
                return "info";
        }
    }

    /**
     * Format movement date
     */
    public String formatMovementDate(InventoryMovement movement) {
        if (movement.getMovementDate() == null) {
            return "N/A";
        }
        return movement.getMovementDate().format(DATE_TIME_FORMATTER);
    }

    /**
     * Get batch number from movement
     */
    public String getBatchNumber(InventoryMovement movement) {
        if (movement.getBatch() == null) {
            return "N/A";
        }
        return movement.getBatch().getBatchNumber();
    }

    /**
     * Get branch name from movement
     */
    public String getBranchName(InventoryMovement movement) {
        if (movement.getBranch() == null) {
            return "N/A";
        }
        return movement.getBranch().getBranchName();
    }

    /**
     * Get user name from movement
     */
    public String getUserName(InventoryMovement movement) {
        if (movement.getUser() == null) {
            return "N/A";
        }
        return movement.getUser().getFirstName() + " " + movement.getUser().getLastName();
    }

    /**
     * Inner class to represent a KARDEX row with calculated balance
     */
    @Data
    public static class KardexRow implements Serializable {
        private InventoryMovement movement;
        private int effectiveQuantity;
        private int runningBalance;
    }
}
