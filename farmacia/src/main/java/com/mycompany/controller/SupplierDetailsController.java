package com.mycompany.controller;

import com.mycompany.model.entity.Supplier;
import com.mycompany.model.entity.SupplierContact;
import com.mycompany.service.ISupplierService;
import com.mycompany.service.ISupplierContactService;
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
import java.util.Map;

/**
 * JSF Controller for Supplier details view.
 * Displays complete information about a specific supplier.
 */
@Data
@Named(value = "supplierDetailsController")
@ViewScoped
public class SupplierDetailsController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ISupplierService supplierService;

    @EJB
    private ISupplierContactService contactService;

    @Inject
    private UserController userController;

    private Supplier supplier;
    private List<SupplierContact> contacts;
    private SupplierContact currentContact;

    @PostConstruct
    public void init() {
        // Get supplierId parameter from URL
        Map<String, String> params = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap();

        String supplierIdParam = params.get("supplierId");

        if (supplierIdParam != null && !supplierIdParam.isEmpty()) {
            try {
                Integer supplierId = Integer.valueOf(supplierIdParam);
                loadSupplier(supplierId);
            } catch (NumberFormatException e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "ID de proveedor inválido"));
            }
        }
    }

    /**
     * Load supplier details by ID.
     *
     * @param supplierId the supplier ID to load
     */
    public void loadSupplier(Integer supplierId) {
        try {
            supplier = supplierService.findById(supplierId);
            if (supplier == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Advertencia",
                                "Proveedor no encontrado"));
            } else {
                loadContacts();
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "No se pudo cargar el proveedor: " + e.getMessage()));
        }
    }

    /**
     * Load contacts for the current supplier.
     */
    private void loadContacts() {
        if (supplier != null) {
            try {
                contacts = contactService.findBySupplier(supplier);
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "No se pudieron cargar los contactos: " + e.getMessage()));
            }
        }
    }

    /**
     * Create a new contact for the current supplier.
     */
    public void createNewContact() {
        currentContact = SupplierContact.builder()
                .supplier(supplier)
                .isPrimary(false)
                .build();
    }

    /**
     * Save the current contact (create or update).
     */
    public void saveContact() {
        try {
            if (currentContact.getContactId() == null) {
                // New contact
                contactService.save(currentContact);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Éxito",
                                "Contacto agregado correctamente"));
            } else {
                // Update existing contact
                contactService.edit(currentContact);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Éxito",
                                "Contacto actualizado correctamente"));
            }
            loadContacts();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "No se pudo guardar el contacto: " + e.getMessage()));
        }
    }

    /**
     * Delete the current contact.
     */
    public void deleteContact() {
        try {
            contactService.delete(currentContact);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito",
                            "Contacto eliminado correctamente"));
            loadContacts();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "No se pudo eliminar el contacto: " + e.getMessage()));
        }
    }

    /**
     * Check if current user is admin.
     *
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin() {
        return userController != null && userController.isAdmin();
    }
}
