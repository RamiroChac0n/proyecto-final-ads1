package com.mycompany.controller;

import com.mycompany.model.entity.User;
import com.mycompany.model.entity.enums.Role;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 * JSF Managed Bean controller for application navigation and menu state management.
 * <p>
 * This session-scoped controller centralizes navigation logic and provides menu
 * state tracking for the PrimeFaces TabMenu component. It manages active tab
 * highlighting, page redirects, and delegates authentication checks to UserController.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Provide navigation outcomes for all major application pages</li>
 *   <li>Track and return active tab index for menu highlighting</li>
 *   <li>Detect current page for conditional UI rendering</li>
 *   <li>Delegate authentication and authorization to UserController</li>
 *   <li>Support role-based menu visibility</li>
 * </ul>
 *
 * <h3>Menu Structure:</h3>
 * <ul>
 *   <li><strong>Tab 0 (Home):</strong> Dashboard - All users</li>
 *   <li><strong>Tab 1 (User Management):</strong> User CRUD - ADMIN only</li>
 *   <li><strong>Tab 2 (Product Management):</strong> Product/Batch CRUD - ADMIN only</li>
 *   <li><strong>Tab 3 (Cash Register):</strong> Register operations - ADMIN only</li>
 *   <li><strong>Logout:</strong> Session termination - All users</li>
 * </ul>
 *
 * <h3>JSF Scope:</h3>
 * {@code @SessionScoped} - Maintains navigation state across multiple pages and requests.
 *
 * @author ramir
 * @version 1.0
 * @see UserController
 */
@Named(value = "menuController")
@SessionScoped
public class MenuNavigationController implements Serializable {

    @Inject
    private UserController userController;

    /**
     * Navigates to the home/dashboard page.
     *
     * @return Navigation outcome "home?faces-redirect=true"
     */
    public String navigateToHome() {
        return "home?faces-redirect=true";
    }

    /**
     * Navigates to the user management page (ADMIN only).
     * <p>
     * This navigation target should only be accessible to users with ADMIN role.
     * Access control is enforced via page guards in index.xhtml.
     * </p>
     *
     * @return Navigation outcome "index?faces-redirect=true"
     */
    public String navigateToUserManagement() {
        return "index?faces-redirect=true";
    }

    /**
     * Navigates to the user profile page.
     *
     * @return Navigation outcome "profile?faces-redirect=true"
     */
    public String navigateToProfile() {
        return "profile?faces-redirect=true";
    }

    /**
     * Navigates to the application settings page.
     *
     * @return Navigation outcome "settings?faces-redirect=true"
     */
    public String navigateToSettings() {
        return "settings?faces-redirect=true";
    }

    /**
     * Logs out the current user by delegating to UserController.
     * <p>
     * This method invalidates the HTTP session and redirects to the login page.
     * It serves as a convenient wrapper for menu-based logout actions.
     * </p>
     *
     * @return Navigation outcome "login?faces-redirect=true"
     * @see UserController#logout()
     */
    public String logout() {
        return userController.logout();
    }

    /**
     * Checks if the specified page name is present in the current view ID.
     * <p>
     * This utility method enables conditional UI rendering based on the current page.
     * It performs a substring match on the view ID, allowing flexible page detection.
     * </p>
     *
     * <h4>Usage Example:</h4>
     * <pre>
     * &lt;h:panelGroup rendered="#{menuController.isCurrentPage('products')}"&gt;
     *   Product-specific content
     * &lt;/h:panelGroup&gt;
     * </pre>
     *
     * @param page The page name fragment to search for (e.g., "home", "products")
     * @return {@code true} if current view ID contains the page string, {@code false} otherwise
     */
    public boolean isCurrentPage(String page) {
        String currentViewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        return currentViewId != null && currentViewId.contains(page);
    }

    /**
     * Calculates the active tab index for the admin menu based on current page.
     * <p>
     * This method determines which tab should be highlighted in the admin navigation menu
     * by examining the current view ID. It supports tab highlighting for all admin-accessible
     * pages including detail views (e.g., product-details shows Products tab as active).
     * </p>
     *
     * <h4>Tab Index Mapping:</h4>
     * <ul>
     *   <li><strong>0:</strong> Home (home.xhtml)</li>
     *   <li><strong>1:</strong> User Management (index.xhtml)</li>
     *   <li><strong>2:</strong> Product Management (products.xhtml, product-details.xhtml)</li>
     *   <li><strong>3:</strong> Cash Register (cash-register.xhtml)</li>
     * </ul>
     *
     * <h4>Usage in XHTML:</h4>
     * <pre>
     * &lt;p:tabMenu activeIndex="#{menuController.adminActiveTabIndex}"&gt;
     *   ...
     * &lt;/p:tabMenu&gt;
     * </pre>
     *
     * @return Zero-based tab index (0-3), defaults to 0 (Home) for unknown pages
     */
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

    /**
     * Calculates the active tab index for the user menu with role-based filtering.
     * <p>
     * This method determines which tab should be highlighted in the navigation menu
     * while accounting for the current user's role. Non-admin users may have fewer
     * visible tabs, so this method ensures correct highlighting regardless of role.
     * </p>
     *
     * <p>
     * Unlike {@link #getAdminActiveTabIndex()}, this method checks both the current
     * page AND the user's role to determine the correct active index, since menu items
     * may be conditionally rendered based on permissions.
     * </p>
     *
     * <h4>Tab Index Mapping (Role-Dependent):</h4>
     * <ul>
     *   <li><strong>0:</strong> Home - All users</li>
     *   <li><strong>1:</strong> User Management - ADMIN only</li>
     *   <li><strong>2:</strong> Product Management - ADMIN only</li>
     *   <li><strong>3:</strong> Cash Register - ADMIN only</li>
     * </ul>
     *
     * @return Zero-based tab index, defaults to 0 (Home) for non-admin or unknown pages
     * @see #getAdminActiveTabIndex()
     */
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

    /**
     * Checks if the currently logged-in user has ADMIN role.
     * <p>
     * This is a convenience delegation method that forwards the call to UserController.
     * It allows menu-related XHTML pages to check admin status without directly
     * referencing UserController.
     * </p>
     *
     * @return {@code true} if current user has ADMIN role, {@code false} otherwise
     * @see UserController#isAdmin()
     */
    public boolean isAdmin() {
        return userController.isAdmin();
    }

    /**
     * Retrieves the currently authenticated user from the session.
     * <p>
     * This is a convenience delegation method that forwards the call to UserController.
     * Useful for accessing user information in menu templates.
     * </p>
     *
     * @return The authenticated {@link User} object, or {@code null} if no user is logged in
     * @see UserController#getCurrentUser()
     */
    public User getCurrentUser() {
        return userController.getCurrentUser();
    }

    /**
     * Checks whether a user is currently authenticated in the session.
     * <p>
     * This is a convenience delegation method that forwards the call to UserController.
     * Used for conditional rendering of menu items based on authentication status.
     * </p>
     *
     * @return {@code true} if a user is logged in, {@code false} otherwise
     * @see UserController#isLoggedIn()
     */
    public boolean isLoggedIn() {
        return userController.isLoggedIn();
    }
}