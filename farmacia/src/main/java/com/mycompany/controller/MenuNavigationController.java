package com.mycompany.controller;

import com.mycompany.model.entity.User;
import com.mycompany.model.entity.enums.Role;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 * Controller for handling TabMenu navigation and menu state management
 * @author ramir
 */
@Named(value = "menuController")
@SessionScoped
public class MenuNavigationController implements Serializable {
    
    @Inject
    private UserController userController;
    
    // Navigation methods
    public String navigateToHome() {
        return "home?faces-redirect=true";
    }
    
    public String navigateToUserManagement() {
        return "index?faces-redirect=true";
    }
    
    public String navigateToProfile() {
        return "profile?faces-redirect=true";
    }
    
    public String navigateToSettings() {
        return "settings?faces-redirect=true";
    }
    
    public String logout() {
        return userController.logout();
    }
    
    // Helper methods for menu state
    public boolean isCurrentPage(String page) {
        String currentViewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        return currentViewId != null && currentViewId.contains(page);
    }
    
    // Get active tab index for admin menu (index.xhtml and products.xhtml)
    public int getAdminActiveTabIndex() {
        if (isCurrentPage("home")) {
            return 0;
        } else if (isCurrentPage("index")) {
            return 1; // User Management
        } else if (isCurrentPage("products") || isCurrentPage("product-details")) {
            return 2; // Product Management
        } else if (isCurrentPage("cash-register")) {
            return 3; // Cash Register
        }
        return 0;
    }

    // Get active tab index for user menu (used in home.xhtml and cash-register.xhtml)
    public int getUserActiveTabIndex() {
        User currentUser = userController.getCurrentUser();
        boolean isAdmin = currentUser != null && Role.ADMIN.equals(currentUser.getRole());

        if (isCurrentPage("home")) {
            return 0; // Home
        } else if (isCurrentPage("index") && isAdmin) {
            return 1; // User Management
        } else if ((isCurrentPage("products") || isCurrentPage("product-details")) && isAdmin) {
            return 2; // Product Management
        } else if (isCurrentPage("cash-register") && isAdmin) {
            return 3; // Cash Register
        }

        return 0; // Default to Home
    }
    
    // Check if current user is admin (delegating to UserController)
    public boolean isAdmin() {
        return userController.isAdmin();
    }
    
    // Get current user (delegating to UserController)
    public User getCurrentUser() {
        return userController.getCurrentUser();
    }
    
    // Check if user is logged in (delegating to UserController)
    public boolean isLoggedIn() {
        return userController.isLoggedIn();
    }
}