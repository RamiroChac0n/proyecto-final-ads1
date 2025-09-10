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
import java.util.List;
import lombok.Data;
import org.primefaces.PrimeFaces;

/**
 * Controller for Product management
 * @author ramir
 */
@Data
@Named(value = "productController")
@SessionScoped
public class ProductController implements Serializable {

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

    // Product management fields
    private Product product;
    private List<Product> products;
    
    // Dropdown data
    private List<ProductType> productTypes;
    private List<ProductCategory> productCategories;
    private List<ActivePrinciple> activePrinciples;
    private List<DosageForm> dosageForms;
    private List<ConcentrationUnit> concentrationUnits;

    @PostConstruct
    public void init() {
        refreshProducts();
        loadDropdownData();
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
    
    // Page access check for admin-only pages
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
    
    public void refreshProducts() {
        products = productService.list();
    }
    
    public void loadDropdownData() {
        productTypes = productTypeService.findActiveTypes();
        productCategories = productCategoryService.findActiveCategories();
        activePrinciples = activePrincipleService.findAllOrderedByName();
        dosageForms = dosageFormService.findAllOrderedByName();
        concentrationUnits = concentrationUnitService.findAllOrderedByName();
    }
    
    public void createNew() {
        product = Product.builder()
                .requiresPrescription(false)
                .minStock(0)
                .maxStock(1000)
                .currentStock(0)
                .isActive(true)
                .build();
    }

    public List<Product> getProducts() {
        if (products == null) {
            products = productService.list();
        }
        return products;
    }

    public void save() {
        try {
            Product existing = productService.findById(product.getProductId());
            if (existing == null) {
                productService.save(product);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Product added successfully"));
            } else {
                productService.edit(product);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Product updated successfully"));
            }
            refreshProducts();
            createNew();
            PrimeFaces.current().executeScript("PF('dlgProductRegister').hide()");
            PrimeFaces.current().ajax().update("form:messages", "form:dt-products");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to save product: " + e.getMessage()));
        }
    }

    public void delete() {
        try {
            productService.delete(product);
            refreshProducts();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Product deleted successfully"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete product: " + e.getMessage()));
        }
        
        PrimeFaces.current().executeScript("PF('dlgDeleteProduct').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:dt-products");
    }
}