package com.mycompany.controller;

import com.mycompany.model.entity.Supplier;
import com.mycompany.service.ISupplierService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * JSF Managed Bean controller for supplier management (ADMIN only).
 * <p>
 * This view-scoped controller manages supplier CRUD operations including creating new
 * suppliers, updating existing supplier information, and deleting suppliers. Suppliers
 * represent the vendors or distributors from whom the pharmacy purchases products.
 * </p>
 *
 * <h3>Core Functionality:</h3>
 * <ul>
 *   <li><strong>Create:</strong> Initialize new supplier with default values (country="Guatemala", active=true)</li>
 *   <li><strong>Read:</strong> Display list of all suppliers in DataTable</li>
 *   <li><strong>Update:</strong> Modify existing supplier information</li>
 *   <li><strong>Delete:</strong> Remove supplier from system (soft delete if referenced)</li>
 * </ul>
 *
 * <h3>Supplier Information:</h3>
 * <ul>
 *   <li>Supplier ID (auto-generated)</li>
 *   <li>Company name, trade name, contact person</li>
 *   <li>Address, phone, email, website</li>
 *   <li>Country (defaults to "Guatemala")</li>
 *   <li>NIT (Tax Identification Number)</li>
 *   <li>Active status flag</li>
 * </ul>
 *
 * <h3>Access Control:</h3>
 * <p>
 * Only ADMIN users can manage suppliers. The controller delegates authorization
 * checks to {@link UserController#checkAdminAccess()}.
 * </p>
 *
 * <h3>UI Features:</h3>
 * <ul>
 *   <li>DataTable with search, filter, pagination, sorting</li>
 *   <li>Create/Edit dialog with form validation</li>
 *   <li>Delete confirmation dialog</li>
 *   <li>Link to supplier details page</li>
 *   <li>Active/inactive status indicators</li>
 * </ul>
 *
 * @author ramir
 * @version 1.0
 * @see Supplier
 * @see ISupplierService
 * @see SupplierDetailsController
 */
@Data
@Named(value = "supplierController")
@ViewScoped
public class SupplierController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ISupplierService supplierService;

    @Inject
    private UserController userController;

    private Supplier supplier;
    private List<Supplier> suppliers;

    /**
     * Initializes the controller after dependency injection is complete.
     * <p>
     * This method loads all suppliers from the database for display in the
     * DataTable on initial page render.
     * </p>
     *
     * @see #loadSuppliers()
     */
    @PostConstruct
    public void init() {
        loadSuppliers();
    }

    /**
     * Loads all suppliers from the database for display in the DataTable.
     * <p>
     * This method retrieves the complete list of suppliers (both active and inactive)
     * and handles any database errors gracefully by displaying an error message to the user.
     * </p>
     *
     * <h4>Side Effects:</h4>
     * <ul>
     *   <li>Sets the {@code suppliers} field with the retrieved list</li>
     *   <li>Displays error message if loading fails</li>
     * </ul>
     *
     * <h4>Error Handling:</h4>
     * <p>
     * Catches all exceptions and displays user-friendly error message in Spanish.
     * Does not rethrow exceptions to allow page to render even if load fails.
     * </p>
     *
     * @see ISupplierService#list()
     */
    private void loadSuppliers() {
        try {
            suppliers = supplierService.list();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "No se pudieron cargar los proveedores: " + e.getMessage()));
        }
    }

    /**
     * Creates a new supplier instance with default values for the create/edit form.
     * <p>
     * This method is called when the user clicks the "New Supplier" button to open
     * the create dialog. It initializes a new {@link Supplier} entity using the
     * builder pattern with sensible defaults:
     * </p>
     * <ul>
     *   <li><strong>country:</strong> "Guatemala" (most common country for this pharmacy system)</li>
     *   <li><strong>isActive:</strong> true (new suppliers are active by default)</li>
     * </ul>
     *
     * <h4>Side Effects:</h4>
     * <p>
     * Sets the {@code supplier} field to a new Supplier instance ready for user input.
     * </p>
     *
     * @see Supplier.SupplierBuilder
     */
    public void createNew() {
        supplier = Supplier.builder()
                .country("Guatemala")
                .isActive(true)
                .build();
    }

    /**
     * Saves the supplier entity to the database (create or update operation).
     * <p>
     * This method determines whether to create a new supplier or update an existing
     * one based on the presence of a supplierId. After successful save, it refreshes
     * the supplier list and displays a success message.
     * </p>
     *
     * <h4>Operation Logic:</h4>
     * <ul>
     *   <li><strong>supplierId = null:</strong> Creates new supplier via {@link ISupplierService#save(Supplier)}</li>
     *   <li><strong>supplierId ≠ null:</strong> Updates existing supplier via {@link ISupplierService#edit(Supplier)}</li>
     * </ul>
     *
     * <h4>Success Flow:</h4>
     * <ol>
     *   <li>Persists supplier to database</li>
     *   <li>Displays success message (Spanish, operation-specific)</li>
     *   <li>Reloads supplier list to show changes</li>
     * </ol>
     *
     * <h4>Error Handling:</h4>
     * <p>
     * Catches all exceptions and displays user-friendly error message. Common errors
     * include validation failures (missing required fields) and constraint violations
     * (duplicate NIT).
     * </p>
     *
     * @see ISupplierService#save(Supplier)
     * @see ISupplierService#edit(Supplier)
     * @see #loadSuppliers()
     */
    public void save() {
        try {
            if (supplier.getSupplierId() == null) {
                supplierService.save(supplier);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Éxito",
                                "Proveedor creado correctamente"));
            } else {
                supplierService.edit(supplier);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Éxito",
                                "Proveedor actualizado correctamente"));
            }
            loadSuppliers();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "No se pudo guardar el proveedor: " + e.getMessage()));
        }
    }

    /**
     * Deletes the selected supplier from the database.
     * <p>
     * This method attempts to delete the supplier. If the supplier is referenced by
     * purchase orders or receipts, the service layer may perform a soft delete (setting
     * isActive=false) instead of a hard delete to maintain referential integrity.
     * </p>
     *
     * <h4>Success Flow:</h4>
     * <ol>
     *   <li>Deletes supplier via {@link ISupplierService#delete(Supplier)}</li>
     *   <li>Displays success message in Spanish</li>
     *   <li>Reloads supplier list to remove deleted entry</li>
     * </ol>
     *
     * <h4>Error Handling:</h4>
     * <p>
     * Catches all exceptions and displays user-friendly error message. Common errors
     * include foreign key constraints if supplier has related purchase orders.
     * </p>
     *
     * <h4>UI Integration:</h4>
     * <p>
     * Typically called from a delete confirmation dialog after user confirms deletion.
     * </p>
     *
     * @see ISupplierService#delete(Supplier)
     * @see #loadSuppliers()
     */
    public void delete() {
        try {
            supplierService.delete(supplier);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito",
                            "Proveedor eliminado correctamente"));
            loadSuppliers();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "No se pudo eliminar el proveedor: " + e.getMessage()));
        }
    }

    /**
     * Checks if the current user has ADMIN role.
     * <p>
     * This is a convenience method that delegates to {@link UserController#isAdmin()}.
     * It is used in the XHTML view to conditionally render admin-only UI components
     * such as create, edit, and delete buttons.
     * </p>
     *
     * <h4>Usage in XHTML:</h4>
     * <pre>
     * &lt;p:commandButton value="New Supplier"
     *                  rendered="#{supplierController.admin}"
     *                  onclick="PF('dlgSupplier').show()" /&gt;
     * </pre>
     *
     * @return {@code true} if current user is ADMIN, {@code false} otherwise
     * @see UserController#isAdmin()
     */
    public boolean isAdmin() {
        return userController != null && userController.isAdmin();
    }

    /**
     * Enforces ADMIN access requirement for the supplier management page.
     * <p>
     * This method should be called as a preRenderView event listener in the XHTML page
     * to prevent non-ADMIN users from accessing the supplier management functionality.
     * If the user is not an ADMIN, they will be redirected to an appropriate page
     * (typically home or unauthorized page).
     * </p>
     *
     * <h4>Usage in XHTML:</h4>
     * <pre>
     * &lt;f:event type="preRenderView" listener="#{supplierController.checkAdminAccess}" /&gt;
     * </pre>
     *
     * @see UserController#checkAdminAccess()
     */
    public void checkAdminAccess() {
        if (userController != null) {
            userController.checkAdminAccess();
        }
    }
}
