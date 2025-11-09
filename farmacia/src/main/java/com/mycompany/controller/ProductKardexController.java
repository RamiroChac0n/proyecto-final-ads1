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
 * JSF Managed Bean controller for displaying Product KARDEX (Inventory Movement History).
 * <p>
 * This view-scoped controller manages the traditional accounting KARDEX format that shows
 * the complete inventory movement history for a single product. The KARDEX displays three
 * main sections: ENTRADAS (inputs), SALIDAS (outputs), and SALDOS (running balance).
 * </p>
 *
 * <h3>KARDEX Structure:</h3>
 * <ul>
 *   <li><strong>ENTRADAS (Inputs):</strong> Quantity, Unit Cost, Total Cost for incoming inventory</li>
 *   <li><strong>SALIDAS (Outputs):</strong> Quantity, Unit Cost, Total Cost for outgoing inventory</li>
 *   <li><strong>SALDOS (Balance):</strong> Running total of Quantity and Total Cost after each movement</li>
 * </ul>
 *
 * <h3>Movement Type Classification:</h3>
 * <ul>
 *   <li><strong>IN:</strong> Treated as ENTRADAS (purchases, receipts)</li>
 *   <li><strong>OUT:</strong> Treated as SALIDAS (sales, returns)</li>
 *   <li><strong>ADJUSTMENT:</strong> Direction determined by sign (positive=ENTRADAS, negative=SALIDAS)</li>
 * </ul>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li>Product selector dropdown to switch between product KARDEXes</li>
 *   <li>Chronological display (oldest movements first)</li>
 *   <li>Running balance calculation with quantity and total cost</li>
 *   <li>Movement details: date, type, batch, branch, user, reason</li>
 *   <li>Color-coded movement types (IN=green, OUT=red, ADJUSTMENT=yellow)</li>
 *   <li>Currency formatting with thousands separator (Q #,###.##)</li>
 * </ul>
 *
 * <h3>Access Control:</h3>
 * <p>
 * Authenticated users only. If no user is logged in, redirects to login page.
 * Requires valid productId parameter in URL, otherwise redirects to products list.
 * </p>
 *
 * @author ramir
 * @version 1.0
 * @see InventoryMovement
 * @see MovementType
 * @see IInventoryMovementService
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

    /**
     * Initializes the controller after dependency injection is complete.
     * <p>
     * This method performs the following initialization sequence:
     * </p>
     * <ol>
     *   <li>Loads all active products for the product selector dropdown</li>
     *   <li>Extracts productId from URL query parameter ("productId")</li>
     *   <li>If productId is valid:
     *     <ul>
     *       <li>Loads the product entity from database</li>
     *       <li>Loads all inventory movements for the product</li>
     *       <li>Calculates KARDEX rows with running balance</li>
     *       <li>Synchronizes selectedProduct with loaded product</li>
     *     </ul>
     *   </li>
     *   <li>If productId is invalid or missing, redirects to products list page</li>
     * </ol>
     *
     * <h4>Error Handling:</h4>
     * <ul>
     *   <li>NumberFormatException: Redirects to products list if productId is not a valid Long</li>
     *   <li>Missing parameter: Redirects to products list if no productId provided</li>
     * </ul>
     *
     * @see #loadAllProducts()
     * @see #loadProduct()
     * @see #loadMovements()
     * @see #calculateKardex()
     */
    @PostConstruct
    public void init() {
        // LOGGER.info("ProductKardexController.init() - Starting initialization");

        loadAllProducts();

        String productIdParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("productId");

        if (productIdParam != null && !productIdParam.isEmpty()) {
            try {
                productId = Long.valueOf(productIdParam);
                loadProduct();
                loadMovements();
                calculateKardex();

                selectedProduct = product;
            } catch (NumberFormatException e) {
                LOGGER.severe("Invalid productId parameter: " + productIdParam);
                redirectToProducts();
            }
        } else {
            LOGGER.warning("No productId parameter provided");
            redirectToProducts();
        }

        // LOGGER.info("ProductKardexController.init() - Initialization completed");
    }

    /**
     * Verifies that a user is logged in before allowing access to KARDEX page.
     * <p>
     * This method should be called as a preRenderView event listener in the XHTML page
     * to enforce authentication. If no user is logged in, redirects to the login page.
     * </p>
     *
     * <h4>Usage in XHTML:</h4>
     * <pre>
     * &lt;f:event type="preRenderView" listener="#{productKardexController.checkAccess}" /&gt;
     * </pre>
     *
     * @see #isLoggedIn()
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
     * Checks if a user is currently logged in by verifying session state.
     * <p>
     * This method checks for the presence of a "user" object in the HTTP session map.
     * The user object is set by {@code UserController} during successful authentication.
     * </p>
     *
     * @return {@code true} if a user is logged in, {@code false} otherwise
     * @see UserController#login()
     */
    public boolean isLoggedIn() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user") != null;
    }

    /**
     * Retrieves the currently logged-in user from the HTTP session.
     * <p>
     * This method extracts the {@link User} object stored in the session map by
     * {@code UserController} during authentication. Returns {@code null} if no user
     * is logged in.
     * </p>
     *
     * @return The current {@link User} object, or {@code null} if not logged in
     * @see UserController#getCurrentUser()
     * @see #isLoggedIn()
     */
    public User getCurrentUser() {
        return (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
    }

    /**
     * Redirects the user to the products list page (products.xhtml).
     * <p>
     * This method is called when:
     * </p>
     * <ul>
     *   <li>No productId parameter is provided in the URL</li>
     *   <li>The productId parameter is invalid (not a valid Long)</li>
     *   <li>The product with the given ID is not found in the database</li>
     * </ul>
     *
     * <h4>Error Handling:</h4>
     * <p>
     * Logs severe error if redirection fails, but does not throw exception.
     * </p>
     */
    private void redirectToProducts() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("products.xhtml");
        } catch (Exception e) {
            LOGGER.severe("Error redirecting to products: " + e.getMessage());
        }
    }

    /**
     * Loads the product entity from the database using the productId field.
     * <p>
     * This method performs product lookup and validation:
     * </p>
     * <ol>
     *   <li>Queries the database for the product with the specified ID</li>
     *   <li>If product not found:
     *     <ul>
     *       <li>Logs warning with product ID</li>
     *       <li>Displays error message to user</li>
     *       <li>Redirects to products list page</li>
     *     </ul>
     *   </li>
     *   <li>If found, sets the product field for use by other methods</li>
     * </ol>
     *
     * @see IProductService#findById(Long)
     * @see #redirectToProducts()
     */
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

    /**
     * Loads inventory movements for the current product filtered by user's branch.
     * <p>
     * This method queries the database for {@link InventoryMovement} records
     * associated with the current product, filtered by the user's assigned branch
     * for multi-branch inventory segregation. If the user has no branch assigned,
     * all movements are shown (fallback behavior). The movements are stored in
     * chronological order and used by {@link #calculateKardex()} to generate KARDEX rows.
     * </p>
     *
     * <h4>Branch Filtering:</h4>
     * <ul>
     *   <li><strong>User with branch:</strong> Shows only movements from their branch</li>
     *   <li><strong>User without branch:</strong> Shows all movements (fallback)</li>
     * </ul>
     *
     * <h4>Side Effects:</h4>
     * <p>
     * Sets the {@code movements} field with the retrieved list. Logs the count of
     * movements loaded for debugging purposes.
     * </p>
     *
     * @see IInventoryMovementService#findByBranchAndProduct(Branch, Product)
     * @see IInventoryMovementService#findByProduct(Product)
     * @see #calculateKardex()
     */
    private void loadMovements() {
        if (product != null) {
            User currentUser = getCurrentUser();

            // Filter movements by user's branch
            if (currentUser != null && currentUser.getBranch() != null) {
                movements = inventoryMovementService.findByBranchAndProduct(
                    currentUser.getBranch(), product);
            } else {
                // Fallback: show all movements if user has no branch assigned
                movements = inventoryMovementService.findByProduct(product);
            }
            // LOGGER.info("Loaded " + movements.size() + " movements for product: " + product.getCommercialName());
        }
    }

    /**
     * Loads all active products from the database for the product selector dropdown.
     * <p>
     * This method populates the {@code allProducts} list used by the dropdown component
     * in the XHTML view. Users can select a different product from this list to view
     * its KARDEX without navigating back to the products list page.
     * </p>
     *
     * <h4>Side Effects:</h4>
     * <p>
     * Sets the {@code allProducts} field. Logs the count of products loaded for
     * debugging purposes.
     * </p>
     *
     * @see IProductService#list()
     * @see #onProductChange()
     */
    private void loadAllProducts() {
        allProducts = productService.list();
        // LOGGER.info("Loaded " + (allProducts != null ? allProducts.size() : 0) + " products for selector");
    }

    /**
     * Handles product selection change from the dropdown selector.
     * <p>
     * When a user selects a different product from the dropdown, this method triggers
     * a page redirect to the KARDEX page for the newly selected product. This allows
     * users to quickly switch between product KARDEXes without returning to the products
     * list page.
     * </p>
     *
     * <h4>Process Flow:</h4>
     * <ol>
     *   <li>Validates that selectedProduct and its ID are not null</li>
     *   <li>Constructs URL: "product-kardex.xhtml?productId={productId}"</li>
     *   <li>Performs HTTP redirect to the new URL</li>
     *   <li>On error: Displays error message to user and logs error</li>
     * </ol>
     *
     * <h4>AJAX Integration:</h4>
     * <p>
     * This method is typically called via AJAX from a PrimeFaces selectOneMenu or
     * autoComplete component with {@code p:ajax} listener.
     * </p>
     *
     * @see Product#getProductId()
     */
    public void onProductChange() {
        if (selectedProduct != null && selectedProduct.getProductId() != null) {
            try {
                String url = "product-kardex.xhtml?productId=" + selectedProduct.getProductId();
                FacesContext.getCurrentInstance().getExternalContext().redirect(url);
                // LOGGER.info("Redirecting to KARDEX for product: " + selectedProduct.getCommercialName());
            } catch (Exception e) {
                LOGGER.severe("Error redirecting to product KARDEX: " + e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not load product KARDEX"));
            }
        }
    }

    /**
     * Calculates KARDEX rows with running balance using traditional accounting format.
     * <p>
     * This method transforms the flat list of {@link InventoryMovement} records into
     * structured {@link KardexRow} objects that display inputs, outputs, and running balance
     * in the three-column KARDEX format (ENTRADAS | SALIDAS | SALDOS).
     * </p>
     *
     * <h4>Calculation Logic:</h4>
     * <ol>
     *   <li>Reverses movements list to process from oldest to newest</li>
     *   <li>For each movement:
     *     <ul>
     *       <li>Extracts unit cost from associated batch (defaults to 0 if no batch)</li>
     *       <li>Calculates total cost: unitCost × quantity</li>
     *       <li>Determines if movement is INPUT or OUTPUT:
     *         <ul>
     *           <li><strong>MovementType.IN:</strong> Always treated as INPUT</li>
     *           <li><strong>MovementType.OUT:</strong> Always treated as OUTPUT</li>
     *           <li><strong>MovementType.ADJUSTMENT:</strong> Positive quantity = INPUT, negative = OUTPUT</li>
     *         </ul>
     *       </li>
     *       <li>Updates running balance:
     *         <ul>
     *           <li>INPUT: Add quantity and total cost to balance</li>
     *           <li>OUTPUT: Subtract quantity and total cost from balance</li>
     *         </ul>
     *       </li>
     *       <li>Populates KardexRow with INPUT/OUTPUT columns and current BALANCE</li>
     *     </ul>
     *   </li>
     *   <li>Maintains chronological order (oldest first) for table display</li>
     * </ol>
     *
     * <h4>Side Effects:</h4>
     * <p>
     * Sets the {@code kardexRows} field with the calculated list of KardexRow objects.
     * This list is bound to the DataTable in the XHTML view.
     * </p>
     *
     * @see KardexRow
     * @see InventoryMovement
     * @see MovementType
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
     * Returns the PrimeIcons CSS class for the specified movement type.
     * <p>
     * This method provides visual iconography for each movement type in the UI,
     * making it easier for users to quickly identify the type of inventory movement.
     * </p>
     *
     * <h4>Icon Mapping:</h4>
     * <ul>
     *   <li><strong>IN:</strong> "pi pi-arrow-down" (downward arrow, inventory coming in)</li>
     *   <li><strong>OUT:</strong> "pi pi-arrow-up" (upward arrow, inventory going out)</li>
     *   <li><strong>ADJUSTMENT:</strong> "pi pi-sync" (sync/refresh icon, manual adjustment)</li>
     *   <li><strong>null or unknown:</strong> "pi pi-question" (question mark)</li>
     * </ul>
     *
     * @param type The {@link MovementType} to get an icon for
     * @return PrimeIcons CSS class string (e.g., "pi pi-arrow-down")
     * @see MovementType
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
     * Returns the CSS text color class for the specified movement type.
     * <p>
     * This method provides color-coding for movement types using Bootstrap/PrimeFlex
     * text utility classes. Colors help users quickly distinguish between different
     * types of inventory movements.
     * </p>
     *
     * <h4>Color Mapping:</h4>
     * <ul>
     *   <li><strong>IN:</strong> "text-success" (green text, positive action)</li>
     *   <li><strong>OUT:</strong> "text-danger" (red text, negative action)</li>
     *   <li><strong>ADJUSTMENT:</strong> "text-warning" (yellow/orange text, manual intervention)</li>
     *   <li><strong>null or unknown:</strong> "" (empty string, default color)</li>
     * </ul>
     *
     * @param type The {@link MovementType} to get a CSS class for
     * @return Bootstrap/PrimeFlex CSS class string (e.g., "text-success")
     * @see MovementType
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
     * Returns the PrimeFaces badge severity for the specified movement type.
     * <p>
     * This method provides severity levels for PrimeFaces badge components, allowing
     * movement types to be displayed with appropriate background colors in badges.
     * </p>
     *
     * <h4>Severity Mapping:</h4>
     * <ul>
     *   <li><strong>IN:</strong> "success" (green badge)</li>
     *   <li><strong>OUT:</strong> "danger" (red badge)</li>
     *   <li><strong>ADJUSTMENT:</strong> "warning" (yellow/orange badge)</li>
     *   <li><strong>null or unknown:</strong> "info" (blue badge)</li>
     * </ul>
     *
     * <h4>Usage in XHTML:</h4>
     * <pre>
     * &lt;p:badge value="#{movement.movementType.name()}"
     *          severity="#{productKardexController.getMovementTypeSeverity(movement.movementType)}" /&gt;
     * </pre>
     *
     * @param type The {@link MovementType} to get severity for
     * @return PrimeFaces severity string ("success", "danger", "warning", or "info")
     * @see MovementType
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
     * Returns a user-friendly Spanish label for the movement type.
     * <p>
     * This method is used primarily for Excel/PDF exports to provide readable Spanish labels
     * instead of technical enum names. The web interface uses the {@code getMovementTypeIcon()},
     * {@code getMovementTypeClass()}, and {@code getMovementTypeSeverity()} methods to display
     * visual badges with icons.
     * </p>
     * <p>
     * <strong>Usage in XHTML:</strong>
     * <pre>
     * &lt;p:column exportValue="#{productKardexController.getMovementTypeLabel(row.movement.movementType)}"&gt;
     *     ...
     * &lt;/p:column&gt;
     * </pre>
     * </p>
     *
     * @param type The {@link MovementType} to get a label for
     * @return Spanish label string:
     *         <ul>
     *             <li>"ENTRADA" for {@code IN} (incoming inventory)</li>
     *             <li>"SALIDA" for {@code OUT} (outgoing inventory)</li>
     *             <li>"AJUSTE" for {@code ADJUSTMENT} (inventory adjustments)</li>
     *             <li>"N/A" if type is null</li>
     *         </ul>
     * @see MovementType
     */
    public String getMovementTypeLabel(MovementType type) {
        if (type == null) return "N/A";

        switch (type) {
            case IN:
                return "ENTRADA";
            case OUT:
                return "SALIDA";
            case ADJUSTMENT:
                return "AJUSTE";
            default:
                return "N/A";
        }
    }

    /**
     * Formats the movement date/time for display in the KARDEX table.
     * <p>
     * This method converts the {@link java.time.LocalDateTime} from the movement record
     * into a user-friendly string format using the pattern "dd/MM/yyyy HH:mm:ss"
     * (e.g., "15/01/2025 14:30:45").
     * </p>
     *
     * @param movement The {@link InventoryMovement} to extract the date from
     * @return Formatted date/time string (e.g., "15/01/2025 14:30:45"), or "N/A" if movement date is null
     * @see #DATE_TIME_FORMATTER
     */
    public String formatMovementDate(InventoryMovement movement) {
        if (movement.getMovementDate() == null) {
            return "N/A";
        }
        return movement.getMovementDate().format(DATE_TIME_FORMATTER);
    }

    /**
     * Extracts the batch number from an inventory movement for display.
     * <p>
     * This method retrieves the batch number associated with the movement's product batch.
     * Some movements may not have an associated batch (e.g., adjustments), in which case
     * "N/A" is returned.
     * </p>
     *
     * @param movement The {@link InventoryMovement} to extract the batch number from
     * @return The batch number string, or "N/A" if no batch is associated with the movement
     * @see com.mycompany.model.entity.ProductBatch#getBatchNumber()
     */
    public String getBatchNumber(InventoryMovement movement) {
        if (movement.getBatch() == null) {
            return "N/A";
        }
        return movement.getBatch().getBatchNumber();
    }

    /**
     * Extracts the branch name from an inventory movement for display.
     * <p>
     * This method retrieves the name of the branch/location where the inventory movement
     * occurred. Some movements may not have an associated branch, in which case "N/A"
     * is returned.
     * </p>
     *
     * @param movement The {@link InventoryMovement} to extract the branch name from
     * @return The branch name string, or "N/A" if no branch is associated with the movement
     * @see com.mycompany.model.entity.Branch#getBranchName()
     */
    public String getBranchName(InventoryMovement movement) {
        if (movement.getBranch() == null) {
            return "N/A";
        }
        return movement.getBranch().getBranchName();
    }

    /**
     * Extracts the full name of the user who performed an inventory movement.
     * <p>
     * This method constructs the full name by concatenating the user's first name and
     * last name. Some movements may not have an associated user (e.g., automated system
     * adjustments), in which case "N/A" is returned.
     * </p>
     *
     * @param movement The {@link InventoryMovement} to extract the user name from
     * @return The user's full name (e.g., "Juan Pérez"), or "N/A" if no user is associated
     * @see User#getFirstName()
     * @see User#getLastName()
     */
    public String getUserName(InventoryMovement movement) {
        if (movement.getUser() == null) {
            return "N/A";
        }
        return movement.getUser().getFirstName() + " " + movement.getUser().getLastName();
    }

    /**
     * Formats a monetary amount as Guatemalan Quetzales with thousands separator.
     * <p>
     * This method formats {@link BigDecimal} amounts into the Guatemalan currency format
     * using the pattern "Q #,###.##" where:
     * </p>
     * <ul>
     *   <li>Q is the currency symbol for Guatemalan Quetzal</li>
     *   <li>Commas separate thousands (1,000.00)</li>
     *   <li>Two decimal places are always shown</li>
     * </ul>
     *
     * <h4>Examples:</h4>
     * <ul>
     *   <li>formatCurrency(BigDecimal.valueOf(1234.56)) → "Q 1,234.56"</li>
     *   <li>formatCurrency(BigDecimal.ZERO) → "Q 0.00"</li>
     *   <li>formatCurrency(null) → "Q 0.00"</li>
     * </ul>
     *
     * @param amount The {@link BigDecimal} amount to format, may be null
     * @return Formatted currency string (e.g., "Q 1,234.56"), defaults to "Q 0.00" if amount is null
     */
    public String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "Q 0.00";
        }
        return String.format("Q %,.2f", amount);
    }

    /**
     * Data transfer object representing a single row in the KARDEX table.
     * <p>
     * This inner class encapsulates the traditional accounting KARDEX format with three
     * main sections: ENTRADAS (inputs), SALIDAS (outputs), and SALDOS (running balance).
     * Each KardexRow corresponds to one {@link InventoryMovement} but organizes the data
     * into the standardized KARDEX columns for clear financial tracking.
     * </p>
     *
     * <h3>KARDEX Column Structure:</h3>
     * <table border="1">
     *   <tr>
     *     <th>ENTRADAS (Inputs)</th>
     *     <th>SALIDAS (Outputs)</th>
     *     <th>SALDOS (Balance)</th>
     *   </tr>
     *   <tr>
     *     <td>Quantity | Unit Cost | Total Cost</td>
     *     <td>Quantity | Unit Cost | Total Cost</td>
     *     <td>Quantity | Total Cost</td>
     *   </tr>
     * </table>
     *
     * <h3>Usage Pattern:</h3>
     * <ul>
     *   <li>For INPUT movements: ENTRADAS columns are populated, SALIDAS are null</li>
     *   <li>For OUTPUT movements: SALIDAS columns are populated, ENTRADAS are null</li>
     *   <li>SALDOS (balance) columns are ALWAYS populated with running totals</li>
     * </ul>
     *
     * <h3>Field Descriptions:</h3>
     * <ul>
     *   <li><strong>movement:</strong> Reference to the original InventoryMovement entity</li>
     *   <li><strong>inputQuantity/inputUnitCost/inputTotalCost:</strong> ENTRADAS section (incoming inventory)</li>
     *   <li><strong>outputQuantity/outputUnitCost/outputTotalCost:</strong> SALIDAS section (outgoing inventory)</li>
     *   <li><strong>balanceQuantity/balanceTotalCost:</strong> SALDOS section (running totals after this movement)</li>
     * </ul>
     *
     * @see InventoryMovement
     * @see #calculateKardex()
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
