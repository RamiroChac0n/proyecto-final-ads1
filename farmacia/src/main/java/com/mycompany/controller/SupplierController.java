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
 * JSF Controller for Supplier management.
 * Handles supplier CRUD operations and view interactions.
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

    @PostConstruct
    public void init() {
        loadSuppliers();
    }

    /**
     * Load all suppliers from the database.
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
     * Create a new supplier instance for the form.
     */
    public void createNew() {
        supplier = Supplier.builder()
                .country("Guatemala")
                .isActive(true)
                .build();
    }

    /**
     * Save a supplier (create or update).
     */
    public void save() {
        try {
            if (supplier.getSupplierId() == null) {
                // New supplier
                supplierService.save(supplier);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Éxito",
                                "Proveedor creado correctamente"));
            } else {
                // Update existing supplier
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
     * Delete a supplier.
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
     * Check if current user is admin.
     * Required for rendering admin-only components.
     *
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin() {
        return userController != null && userController.isAdmin();
    }

    /**
     * Check admin access for the page.
     * Redirects to home if user is not admin.
     */
    public void checkAdminAccess() {
        if (userController != null) {
            userController.checkAdminAccess();
        }
    }
}
