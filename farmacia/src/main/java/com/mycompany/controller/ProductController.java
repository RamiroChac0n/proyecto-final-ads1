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
 * Controller for Product management
 * @author ramir
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
        LOGGER.info("ProductController.init() - Starting initialization");
        
        // Check EJB injection
        LOGGER.info("Checking EJB injections:");
        LOGGER.info("  productService: " + (productService != null ? "OK" : "NULL"));
        LOGGER.info("  productTypeService: " + (productTypeService != null ? "OK" : "NULL"));
        LOGGER.info("  productCategoryService: " + (productCategoryService != null ? "OK" : "NULL"));
        LOGGER.info("  activePrincipleService: " + (activePrincipleService != null ? "OK" : "NULL"));
        LOGGER.info("  dosageFormService: " + (dosageFormService != null ? "OK" : "NULL"));
        LOGGER.info("  concentrationUnitService: " + (concentrationUnitService != null ? "OK" : "NULL"));
        
        refreshProducts();
        loadDropdownData();
        
        LOGGER.info("ProductController.init() - Initialization completed");
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
        LOGGER.info("ProductController.loadDropdownData() - Starting data loading");
        
        try {
            productTypes = productTypeService.findActiveTypes();
            LOGGER.info("  Loaded productTypes: " + (productTypes != null ? productTypes.size() + " items" : "NULL"));
            
            productCategories = productCategoryService.findActiveCategories();
            LOGGER.info("  Loaded productCategories: " + (productCategories != null ? productCategories.size() + " items" : "NULL"));
            
            activePrinciples = activePrincipleService.findAllOrderedByName();
            LOGGER.info("  Loaded activePrinciples: " + (activePrinciples != null ? activePrinciples.size() + " items" : "NULL"));
            
            dosageForms = dosageFormService.findAllOrderedByName();
            LOGGER.info("  Loaded dosageForms: " + (dosageForms != null ? dosageForms.size() + " items" : "NULL"));
            
            concentrationUnits = concentrationUnitService.findAllOrderedByName();
            LOGGER.info("  Loaded concentrationUnits: " + (concentrationUnits != null ? concentrationUnits.size() + " items" : "NULL"));
            
            LOGGER.info("ProductController.loadDropdownData() - Data loading completed successfully");
        } catch (Exception e) {
            LOGGER.severe("ProductController.loadDropdownData() - Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void createNew() {
        LOGGER.info("ProductController.createNew() - Creating new product");
        
        product = Product.builder()
                .requiresPrescription(false)
                .minStock(0)
                .maxStock(1000)
                .isActive(true)
                .build();
                
        LOGGER.info("ProductController.createNew() - New product created: " + (product != null ? "OK" : "NULL"));
        LOGGER.info("ProductController.createNew() - Dropdown data available:");
        LOGGER.info("  productTypes: " + (productTypes != null ? productTypes.size() + " items" : "NULL"));
        LOGGER.info("  productCategories: " + (productCategories != null ? productCategories.size() + " items" : "NULL"));
        LOGGER.info("  activePrinciples: " + (activePrinciples != null ? activePrinciples.size() + " items" : "NULL"));
        LOGGER.info("  dosageForms: " + (dosageForms != null ? dosageForms.size() + " items" : "NULL"));
        LOGGER.info("  concentrationUnits: " + (concentrationUnits != null ? concentrationUnits.size() + " items" : "NULL"));
    }

    public List<Product> getProducts() {
        if (products == null) {
            products = productService.list();
        }
        return products;
    }

    public void save() {
        try {
            if (product.getProductId() == null) {
                // New product
                productService.save(product);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Product added successfully"));
            } else {
                // Existing product
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
    
    public String onFlowProcess(FlowEvent event) {
        LOGGER.info("ProductController.onFlowProcess() - Flow from '" + event.getOldStep() + "' to '" + event.getNewStep() + "'");
        return event.getNewStep();
    }
}