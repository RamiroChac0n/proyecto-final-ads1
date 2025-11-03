package com.mycompany.controller;

import com.mycompany.model.entity.*;
import com.mycompany.model.entity.enums.Role;
import com.mycompany.service.*;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import lombok.Data;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FlowEvent;

/**
 * JSF Managed Bean controller for pharmaceutical product catalog management.
 * <p>
 * This session-scoped controller handles CRUD operations for products with full
 * pharmaceutical information including active principles, concentrations, dosage forms,
 * and therapeutic categories. It provides a multi-step wizard interface for product
 * creation and editing.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Product CRUD operations (create, read, update, delete)</li>
 *   <li>Multi-step wizard navigation for product registration</li>
 *   <li>Loading and caching of reference data (types, categories, etc.)</li>
 *   <li>ADMIN-only access enforcement</li>
 *   <li>Integration with multiple lookup services for pharmaceutical data</li>
 * </ul>
 *
 * <h3>Wizard Steps:</h3>
 * <ol>
 *   <li><strong>Basic Information:</strong> Commercial name, brand, manufacturer, product type, category</li>
 *   <li><strong>Pharmaceutical Info:</strong> Active principle, concentration, dosage form, prescription requirement</li>
 *   <li><strong>Inventory Info:</strong> Min/max stock levels, active status</li>
 * </ol>
 *
 * <h3>Access Control:</h3>
 * All product management operations require ADMIN role. Non-admin users are
 * redirected to home page via {@link #checkAdminAccess()}.
 *
 * <h3>JSF Scope:</h3>
 * {@code @SessionScoped} - Maintains state across wizard steps and page navigation.
 *
 * @author ramir
 * @version 1.0
 * @see Product
 * @see IProductService
 * @see ProductType
 * @see ProductCategory
 * @see ActivePrinciple
 * @see DosageForm
 * @see ConcentrationUnit
 */
@Data
@Named(value = "productController")
@SessionScoped
public class ProductController implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(ProductController.class.getName());

    @EJB
    private IProductService productService;

    @EJB
    private IProductTypeService productTypeService;

    @EJB
    private IProductCategoryService productCategoryService;

    @EJB
    private IActivePrincipleService activePrincipleService;

    @EJB
    private IDosageFormService dosageFormService;

    @EJB
    private IConcentrationUnitService concentrationUnitService;

    private Product product;
    private List<Product> products;

    private List<ProductType> productTypes;
    private List<ProductCategory> productCategories;
    private List<ActivePrinciple> activePrinciples;
    private List<DosageForm> dosageForms;
    private List<ConcentrationUnit> concentrationUnits;


    /**
     * Initializes the controller after dependency injection is complete.
     * <p>
     * This method loads initial data required for product management:
     * <ul>
     *   <li>Product list from database</li>
     *   <li>Reference data for all dropdown fields (types, categories, etc.)</li>
     * </ul>
     * Data is cached in session scope for wizard navigation and form population.
     * </p>
     *
     * @see PostConstruct
     */
    @PostConstruct
    public void init() {
        // LOGGER.info("ProductController.init() - Starting initialization");
        // LOGGER.info("Checking EJB injections:");
        // LOGGER.info("  productService: " + (productService != null ? "OK" : "NULL"));
        // LOGGER.info("  productTypeService: " + (productTypeService != null ? "OK" : "NULL"));
        // LOGGER.info("  productCategoryService: " + (productCategoryService != null ? "OK" : "NULL"));
        // LOGGER.info("  activePrincipleService: " + (activePrincipleService != null ? "OK" : "NULL"));
        // LOGGER.info("  dosageFormService: " + (dosageFormService != null ? "OK" : "NULL"));
        // LOGGER.info("  concentrationUnitService: " + (concentrationUnitService != null ? "OK" : "NULL"));

        refreshProducts();
        loadDropdownData();

        // LOGGER.info("ProductController.init() - Initialization completed");
    }

    /**
     * Checks if the currently logged-in user has ADMIN role.
     * <p>
     * Product management operations are restricted to ADMIN users only.
     * This method delegates to UserController's authentication system via session map.
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
     * Page guard that enforces ADMIN-only access to product management pages.
     * <p>
     * This method performs two-level access control:
     * <ol>
     *   <li>First checks if user is logged in - redirects to login page if not</li>
     *   <li>Then checks if user has ADMIN role - redirects to home page if not</li>
     * </ol>
     *
     * Use this guard for product management pages via preRenderView event.
     * </p>
     *
     * <h4>Usage in XHTML:</h4>
     * <pre>
     * &lt;f:event type="preRenderView" listener="#{productController.checkAdminAccess}" /&gt;
     * </pre>
     *
     * @throws IOException if redirect fails (exception is caught and printed to stderr)
     */
    public void checkAdminAccess() {
        if (!isLoggedIn()) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (!isAdmin()) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("home.xhtml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reloads the product list from the database.
     * <p>
     * This method refreshes the products collection after CRUD operations (create, update,
     * delete) to ensure the UI displays the most current data. It is called automatically
     * after save/delete operations.
     * </p>
     */
    public void refreshProducts() {
        products = productService.list();
    }

    /**
     * Loads all reference data required for product form dropdowns.
     * <p>
     * This method populates the following cached collections used in the product wizard:
     * <ul>
     *   <li><strong>Product Types:</strong> Medicine, Supply, Equipment, etc. (active only)</li>
     *   <li><strong>Product Categories:</strong> Therapeutic categories like Analgesic, Antibiotic (active only)</li>
     *   <li><strong>Active Principles:</strong> INN (International Nonproprietary Names) sorted alphabetically</li>
     *   <li><strong>Dosage Forms:</strong> Tablet, Syrup, Injection, etc. sorted alphabetically</li>
     *   <li><strong>Concentration Units:</strong> mg, mL, %, IU sorted alphabetically</li>
     * </ul>
     *
     * Data is loaded once during initialization and cached in session scope for
     * performance. Reference data is filtered to show only active entries where applicable.
     * </p>
     *
     * @see IProductTypeService#findActiveTypes()
     * @see IProductCategoryService#findActiveCategories()
     * @see IActivePrincipleService#findAllOrderedByName()
     * @see IDosageFormService#findAllOrderedByName()
     * @see IConcentrationUnitService#findAllOrderedByName()
     */
    public void loadDropdownData() {
        // LOGGER.info("ProductController.loadDropdownData() - Starting data loading");

        try {
            productTypes = productTypeService.findActiveTypes();
            // LOGGER.info("  Loaded productTypes: " + (productTypes != null ? productTypes.size() + " items" : "NULL"));

            productCategories = productCategoryService.findActiveCategories();
            // LOGGER.info("  Loaded productCategories: " + (productCategories != null ? productCategories.size() + " items" : "NULL"));

            activePrinciples = activePrincipleService.findAllOrderedByName();
            // LOGGER.info("  Loaded activePrinciples: " + (activePrinciples != null ? activePrinciples.size() + " items" : "NULL"));

            dosageForms = dosageFormService.findAllOrderedByName();
            // LOGGER.info("  Loaded dosageForms: " + (dosageForms != null ? dosageForms.size() + " items" : "NULL"));

            concentrationUnits = concentrationUnitService.findAllOrderedByName();
            // LOGGER.info("  Loaded concentrationUnits: " + (concentrationUnits != null ? concentrationUnits.size() + " items" : "NULL"));

            // LOGGER.info("ProductController.loadDropdownData() - Data loading completed successfully");
        } catch (Exception e) {
            LOGGER.severe("ProductController.loadDropdownData() - Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initializes a new blank Product object with default values for the creation wizard.
     * <p>
     * This method is called when opening the product registration wizard to clear any
     * previously selected product data and prepare the form for creating a new product.
     * The new Product object is created with sensible defaults using the Builder pattern:
     * <ul>
     *   <li><strong>requiresPrescription:</strong> {@code false} (can be changed in wizard)</li>
     *   <li><strong>minStock:</strong> 0 units</li>
     *   <li><strong>maxStock:</strong> 1000 units (default inventory threshold)</li>
     *   <li><strong>isActive:</strong> {@code true} (product is active by default)</li>
     * </ul>
     * All other fields (name, brand, type, category, etc.) are initially {@code null}
     * and must be filled through the multi-step wizard.
     * </p>
     *
     * @see Product.ProductBuilder
     */
    public void createNew() {
        // LOGGER.info("ProductController.createNew() - Creating new product");

        product = Product.builder()
                .requiresPrescription(false)
                .minStock(0)
                .maxStock(1000)
                .isActive(true)
                .build();

        // LOGGER.info("ProductController.createNew() - New product created: " + (product != null ? "OK" : "NULL"));
        // LOGGER.info("ProductController.createNew() - Dropdown data available:");
        // LOGGER.info("  productTypes: " + (productTypes != null ? productTypes.size() + " items" : "NULL"));
        // LOGGER.info("  productCategories: " + (productCategories != null ? productCategories.size() + " items" : "NULL"));
        // LOGGER.info("  activePrinciples: " + (activePrinciples != null ? activePrinciples.size() + " items" : "NULL"));
        // LOGGER.info("  dosageForms: " + (dosageForms != null ? dosageForms.size() + " items" : "NULL"));
        // LOGGER.info("  concentrationUnits: " + (concentrationUnits != null ? concentrationUnits.size() + " items" : "NULL"));
    }

    /**
     * Retrieves the list of all products in the catalog with lazy loading.
     * <p>
     * This getter implements lazy loading - if the products collection is null,
     * it automatically loads products from the database. This pattern ensures data
     * is loaded on first access and cached for subsequent requests within the
     * session scope.
     * </p>
     *
     * @return List of all {@link Product} entities in the catalog, never {@code null}
     */
    public List<Product> getProducts() {
        if (products == null) {
            products = productService.list();
        }
        return products;
    }

    /**
     * Saves a new product or updates an existing product in the database.
     * <p>
     * This method performs an upsert operation based on the presence of productId:
     * <ul>
     *   <li>If productId is {@code null}, creates a new product</li>
     *   <li>If productId exists, updates the existing product</li>
     * </ul>
     *
     * After successful save, the product list is refreshed, the form is cleared,
     * the wizard dialog is closed, and UI components are updated via AJAX. On error,
     * an error message is displayed to the user with the exception details.
     * </p>
     *
     * @see IProductService#save(Product)
     * @see IProductService#edit(Product)
     */
    public void save() {
        try {
            if (product.getProductId() == null) {
                // New product
                productService.save(product);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Producto agregado exitosamente"));
            } else {
                // Existing product
                productService.edit(product);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Producto actualizado exitosamente"));
            }
            refreshProducts();
            createNew();
            PrimeFaces.current().executeScript("PF('dlgProductRegister').hide()");
            PrimeFaces.current().ajax().update("form:messages", "form:dt-products");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al guardar el producto: " + e.getMessage()));
        }
    }

    /**
     * Deletes a product from the database.
     * <p>
     * This method removes the selected product from the catalog. After successful deletion,
     * the product list is refreshed, the delete dialog is closed, and UI components are
     * updated via AJAX. A success message is displayed to the user.
     * </p>
     *
     * <p>
     * If deletion fails (e.g., due to foreign key constraints if the product has associated
     * batches or sales), an error message is displayed with the exception details.
     * </p>
     *
     * @see IProductService#delete(Product)
     */
    public void delete() {
        try {
            productService.delete(product);
            refreshProducts();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Producto eliminado exitosamente"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al eliminar el producto: " + e.getMessage()));
        }
        
        PrimeFaces.current().executeScript("PF('dlgDeleteProduct').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:dt-products");
    }

    /**
     * Handles wizard step navigation and validation for the product creation/edit wizard.
     * <p>
     * This method is invoked by PrimeFaces Wizard component when the user navigates between
     * wizard steps (forward or backward). It can be used to perform validation before allowing
     * navigation to the next step.
     * </p>
     *
     * <p>
     * Current implementation allows all navigations without additional validation beyond
     * the JSF validation rules defined in the XHTML form. Custom validation logic can be
     * added here if needed (e.g., validate basic info before allowing navigation to
     * pharmaceutical info step).
     * </p>
     *
     * @param event The {@link FlowEvent} containing information about the navigation
     *              (old step, new step, direction)
     * @return The ID of the step to navigate to. Returning {@code event.getNewStep()}
     *         allows the navigation. Returning {@code event.getOldStep()} would prevent it.
     * @see FlowEvent
     */
    public String onFlowProcess(FlowEvent event) {
        // LOGGER.info("ProductController.onFlowProcess() - Flow from '" + event.getOldStep() + "' to '" + event.getNewStep() + "'");
        return event.getNewStep();
    }
}