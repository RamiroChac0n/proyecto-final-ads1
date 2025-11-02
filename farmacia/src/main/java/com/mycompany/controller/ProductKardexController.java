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
import java.math.BigDecimal;
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
    private List<KardexRow> filteredKardexRows;

    // Product selector fields
    private List<Product> allProducts;
    private Product selectedProduct;

    @PostConstruct
    public void init() {
        LOGGER.info("ProductKardexController.init() - Starting initialization");

        // Load all products for selector dropdown
        loadAllProducts();

        // Get productId from request parameter
        String productIdParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("productId");

        if (productIdParam != null && !productIdParam.isEmpty()) {
            try {
                productId = Long.valueOf(productIdParam);
                loadProduct();
                loadMovements();
                calculateKardex();

                // Synchronize selectedProduct with loaded product
                selectedProduct = product;
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
     * Load all active products for the dropdown selector
     */
    private void loadAllProducts() {
        allProducts = productService.list();
        LOGGER.info("Loaded " + (allProducts != null ? allProducts.size() : 0) + " products for selector");
    }

    /**
     * Handle product selection change from dropdown
     * Redirects to KARDEX page with new product ID
     */
    public void onProductChange() {
        if (selectedProduct != null && selectedProduct.getProductId() != null) {
            try {
                String url = "product-kardex.xhtml?productId=" + selectedProduct.getProductId();
                FacesContext.getCurrentInstance().getExternalContext().redirect(url);
                LOGGER.info("Redirecting to KARDEX for product: " + selectedProduct.getCommercialName());
            } catch (Exception e) {
                LOGGER.severe("Error redirecting to product KARDEX: " + e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not load product KARDEX"));
            }
        }
    }

    /**
     * Calculate KARDEX with running balance (traditional accounting format)
     */
    private void calculateKardex() {
        kardexRows = new ArrayList<>();

        if (movements == null || movements.isEmpty()) {
            return;
        }

        // Reverse the list to calculate from oldest to newest
        List<InventoryMovement> reversedMovements = new ArrayList<>(movements);
        java.util.Collections.reverse(reversedMovements);

        // Running balance trackers
        int runningBalanceQuantity = 0;
        BigDecimal runningBalanceTotalCost = BigDecimal.ZERO;

        for (InventoryMovement movement : reversedMovements) {
            KardexRow row = new KardexRow();
            row.setMovement(movement);

            // Get unit cost from batch (default to 0 if no batch)
            BigDecimal unitCost = BigDecimal.ZERO;
            if (movement.getBatch() != null && movement.getBatch().getUnitCost() != null) {
                unitCost = movement.getBatch().getUnitCost();
            }

            int quantity = Math.abs(movement.getQuantity());
            BigDecimal totalCost = unitCost.multiply(BigDecimal.valueOf(quantity));

            // Determine if this is an INPUT or OUTPUT movement
            boolean isInput = false;
            if (movement.getMovementType() == MovementType.IN) {
                isInput = true;
            } else if (movement.getMovementType() == MovementType.OUT) {
                isInput = false;
            } else { // ADJUSTMENT
                // Positive adjustments are inputs, negative are outputs
                isInput = movement.getQuantity() >= 0;
            }

            // Populate ENTRADAS or SALIDAS columns
            if (isInput) {
                // ENTRADAS (Inputs)
                row.setInputQuantity(quantity);
                row.setInputUnitCost(unitCost);
                row.setInputTotalCost(totalCost);

                // Update running balance (add)
                runningBalanceQuantity += quantity;
                runningBalanceTotalCost = runningBalanceTotalCost.add(totalCost);
            } else {
                // SALIDAS (Outputs)
                row.setOutputQuantity(quantity);
                row.setOutputUnitCost(unitCost);
                row.setOutputTotalCost(totalCost);

                // Update running balance (subtract)
                runningBalanceQuantity -= quantity;
                runningBalanceTotalCost = runningBalanceTotalCost.subtract(totalCost);
            }

            // Set SALDOS (Balance)
            row.setBalanceQuantity(runningBalanceQuantity);
            row.setBalanceTotalCost(runningBalanceTotalCost);

            kardexRows.add(row);
        }

        // Keep chronological order (oldest first)
        // User can change order by clicking on FECHA column header
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
     * Format currency with thousands separator and 2 decimal places
     */
    public String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "Q 0.00";
        }
        return String.format("Q %,.2f", amount);
    }

    /**
     * Inner class to represent a KARDEX row with calculated balance
     */
    @Data
    public static class KardexRow implements Serializable {
        private InventoryMovement movement;

        // ENTRADAS (Inputs)
        private Integer inputQuantity;
        private BigDecimal inputUnitCost;
        private BigDecimal inputTotalCost;

        // SALIDAS (Outputs)
        private Integer outputQuantity;
        private BigDecimal outputUnitCost;
        private BigDecimal outputTotalCost;

        // SALDOS (Balance)
        private Integer balanceQuantity;
        private BigDecimal balanceTotalCost;
    }
}
