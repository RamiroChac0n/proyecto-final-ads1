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

    @Inject
    private UserController userController;

    private Supplier supplier;

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
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "No se pudo cargar el proveedor: " + e.getMessage()));
        }
    }
}
