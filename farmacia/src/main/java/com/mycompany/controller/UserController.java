/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.controller;

import com.mycompany.model.entity.User;
import com.mycompany.model.entity.enums.Role;
import com.mycompany.service.IUserService;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import org.primefaces.PrimeFaces;

/**
 * JSF Managed Bean controller for user authentication and user management operations.
 * <p>
 * This controller is session-scoped to maintain user login state across multiple requests
 * and pages. It handles authentication, authorization, role-based access control, and
 * CRUD operations for user entities.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>User authentication with BCrypt password verification</li>
 *   <li>Session management (login/logout)</li>
 *   <li>Role-based authorization checks (ADMIN, CASHIER, STOREKEEPER)</li>
 *   <li>Page access guards for protected resources</li>
 *   <li>User CRUD operations with business rule validation</li>
 * </ul>
 *
 * <h3>Security Features:</h3>
 * <ul>
 *   <li>Prevents self-deletion of logged-in user</li>
 *   <li>Prevents deletion of last ADMIN user in system</li>
 *   <li>Enforces admin-only access to user management</li>
 *   <li>Session-based authentication state</li>
 * </ul>
 *
 * <h3>JSF Scope:</h3>
 * {@code @SessionScoped} - Maintains state across entire user session.
 * User object persists in session map for authentication validation.
 *
 * @author ramir
 * @version 1.0
 * @see User
 * @see IUserService
 * @see Role
 */
@Data
@Named(value = "userController")
@SessionScoped
public class UserController implements Serializable {

    @EJB
    private IUserService userService;

    private String username;
    private String password;

    private User user;
    private List<User> users;

    /**
     * Authenticates a user with the provided credentials and establishes a session.
     * <p>
     * This method performs BCrypt password verification through the user service.
     * On successful authentication, the user object is stored in the HTTP session map
     * for subsequent authorization checks across requests.
     * </p>
     *
     * @return Navigation outcome string - "home?faces-redirect=true" on success,
     *         {@code null} on authentication failure (stays on login page)
     * @throws IllegalArgumentException if credentials are invalid (handled by service layer)
     * @see IUserService#authenticate(String, String)
     */
    public String login() {
        User authenticatedUser = userService.authenticate(username, password);
        if (authenticatedUser != null) {
            this.user = authenticatedUser;
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("user", user);
            return "home?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Inicio de Sesión", "Credenciales inválidas"));
            return null;
        }
    }

    /**
     * Invalidates the current HTTP session and logs out the user.
     * <p>
     * This method destroys the entire session, removing all session-scoped data
     * including the authenticated user object. The user is redirected to the
     * login page after logout.
     * </p>
     *
     * @return Navigation outcome "login?faces-redirect=true" to redirect to login page
     */
    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        user = null;
        return "login?faces-redirect=true";
    }

    /**
     * Checks whether a user is currently authenticated in the session.
     * <p>
     * This method verifies the presence of a user object in the HTTP session map.
     * It is commonly used in XHTML pages for conditional rendering of UI components
     * and in page guards to enforce authentication requirements.
     * </p>
     *
     * @return {@code true} if a user object exists in session, {@code false} otherwise
     */
    public boolean isLoggedIn() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user") != null;
    }

    /**
     * Page guard that redirects unauthenticated users to the login page.
     * <p>
     * This method should be invoked in XHTML pages using the {@code preRenderView} event
     * to enforce authentication before rendering protected content. If no user is logged in,
     * performs an HTTP redirect to login.xhtml.
     * </p>
     *
     * <h4>Usage in XHTML:</h4>
     * <pre>
     * &lt;f:event type="preRenderView" listener="#{userController.checkLogin}" /&gt;
     * </pre>
     *
     * @throws IOException if redirect fails (exception is caught and printed to stderr)
     */
    public void checkLogin() {
        if (!isLoggedIn()) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Retrieves the currently authenticated user from the session.
     * <p>
     * This method extracts the User object stored in the HTTP session map during
     * the login process. It is used throughout the application for authorization
     * checks and displaying user-specific information.
     * </p>
     *
     * @return The authenticated {@link User} object, or {@code null} if no user is logged in
     */
    public User getCurrentUser() {
        return (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
    }

    /**
     * Checks if the currently logged-in user has ADMIN role.
     * <p>
     * This authorization method is used to control access to administrative functions
     * such as user management, system configuration, and sensitive operations. It performs
     * null-safe role comparison.
     * </p>
     *
     * @return {@code true} if current user is logged in and has {@link Role#ADMIN} role,
     *         {@code false} otherwise
     * @see Role
     */
    public boolean isAdmin() {
        User currentUser = getCurrentUser();
        return currentUser != null && Role.ADMIN.equals(currentUser.getRole());
    }

    /**
     * Checks if the current user has permission to process sales transactions.
     * <p>
     * Sales processing is permitted for users with ADMIN, CASHIER, or STOREKEEPER roles.
     * This method enables role-based UI rendering and access control for sales-related
     * features such as creating sales, viewing sales history, and managing cash registers.
     * </p>
     *
     * @return {@code true} if user has ADMIN, CASHIER, or STOREKEEPER role,
     *         {@code false} otherwise or if not logged in
     * @see Role#ADMIN
     * @see Role#CASHIER
     * @see Role#STOREKEEPER
     */
    public boolean canProcessSales() {
        User currentUser = getCurrentUser();
        return currentUser != null &&
               (Role.ADMIN.equals(currentUser.getRole()) ||
                Role.CASHIER.equals(currentUser.getRole()) ||
                Role.STOREKEEPER.equals(currentUser.getRole()));
    }

    /**
     * Checks if the current user has ADMIN or CASHIER role.
     * <p>
     * This combined authorization check is used for features that require elevated
     * permissions beyond basic storekeeper access, such as viewing detailed sales
     * reports or managing cash register operations.
     * </p>
     *
     * @return {@code true} if user has ADMIN or CASHIER role, {@code false} otherwise
     * @see Role#ADMIN
     * @see Role#CASHIER
     */
    public boolean isAdminOrCashier() {
        User currentUser = getCurrentUser();
        return currentUser != null &&
               (Role.ADMIN.equals(currentUser.getRole()) ||
                Role.CASHIER.equals(currentUser.getRole()));
    }

    /**
     * Checks if the current user has ADMIN or STOREKEEPER role.
     * <p>
     * This authorization check controls access to inventory management features
     * such as product management, batch tracking, purchase orders, and stock
     * movements. Storekeepers have full access to inventory operations but
     * limited access to sales and user management.
     * </p>
     *
     * @return {@code true} if user has ADMIN or STOREKEEPER role, {@code false} otherwise
     * @see Role#ADMIN
     * @see Role#STOREKEEPER
     */
    public boolean isAdminOrStorekeeper() {
        User currentUser = getCurrentUser();
        return currentUser != null &&
               (Role.ADMIN.equals(currentUser.getRole()) ||
                Role.STOREKEEPER.equals(currentUser.getRole()));
    }

    /**
     * Page guard that enforces ADMIN-only access to protected pages.
     * <p>
     * This method performs two-level access control:
     * <ol>
     *   <li>First checks if user is logged in - redirects to login page if not</li>
     *   <li>Then checks if user has ADMIN role - redirects to home page if not</li>
     * </ol>
     *
     * Use this guard for pages that require administrative privileges such as
     * user management, system configuration, and sensitive operations.
     * </p>
     *
     * <h4>Usage in XHTML:</h4>
     * <pre>
     * &lt;f:event type="preRenderView" listener="#{userController.checkAdminAccess}" /&gt;
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
     * Initializes the controller after dependency injection is complete.
     * <p>
     * This method is automatically invoked by the CDI container after all
     * dependencies (like {@code userService}) have been injected. It loads
     * the initial list of users for the user management interface.
     * </p>
     *
     * @see PostConstruct
     */
    @PostConstruct
    public void init() {
        refreshUsers();
    }

    /**
     * Reloads the user list from the database.
     * <p>
     * This method refreshes the users collection after CRUD operations (create, update,
     * delete) to ensure the UI displays the most current data. It is called automatically
     * after save/delete operations and can be invoked manually to refresh stale data.
     * </p>
     */
    public void refreshUsers() {
        users = userService.list();
    }

    /**
     * Initializes a new blank User object for the create/edit form.
     * <p>
     * This method is typically called when opening the user registration dialog
     * to clear any previously selected user data and prepare the form for creating
     * a new user. The new User object will have all fields set to their default values.
     * </p>
     */
    public void createNew() {
        user = new User();
    }

    /**
     * Retrieves the list of all users in the system with lazy loading.
     * <p>
     * This getter implements lazy loading - if the users collection is null,
     * it automatically loads users from the database. This pattern ensures data
     * is loaded on first access and cached for subsequent requests within the
     * session scope.
     * </p>
     *
     * @return List of all {@link User} entities in the system, never {@code null}
     */
    public List<User> getUsers() {
        if (users == null) {
            users = userService.list();
        }
        return users;
    }

    /**
     * Saves a new user or updates an existing user in the database.
     * <p>
     * This method performs an upsert operation:
     * <ul>
     *   <li>If the user ID does not exist in database, creates a new user</li>
     *   <li>If the user ID exists, updates the existing user</li>
     * </ul>
     *
     * Business rules enforced by the service layer include:
     * <ul>
     *   <li>Username auto-generation from first and last name</li>
     *   <li>Password hashing with BCrypt before persistence</li>
     *   <li>Email and phone number validation</li>
     * </ul>
     *
     * After successful save, the user list is refreshed, the form is cleared,
     * the registration dialog is closed, and UI components are updated via AJAX.
     * </p>
     *
     * @see IUserService#save(User)
     * @see IUserService#edit(User)
     */
    public void save() {
        User existing = userService.findById(user.getId());
        if (existing == null) {
            userService.save(user);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Usuario agregado exitosamente"));
        } else {
            userService.edit(user);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Usuario editado exitosamente"));
        }
        refreshUsers();
        createNew();
        PrimeFaces.current().executeScript("PF('dlgUserRegister').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:dt-users");
    }

    /**
     * Deletes a user from the database with business rule validation.
     * <p>
     * This method enforces critical business rules before deletion:
     * <ul>
     *   <li><strong>Self-deletion prevention:</strong> User cannot delete their own account</li>
     *   <li><strong>Last admin protection:</strong> Cannot delete the last ADMIN user in the system</li>
     * </ul>
     *
     * The validation is delegated to {@link IUserService#canDeleteUser(String, User)}
     * which checks these conditions. If deletion is not permitted, an error message
     * is displayed to the user explaining the reason.
     * </p>
     *
     * <p>
     * On successful deletion, the user list is refreshed, the delete dialog is closed,
     * and UI components are updated via AJAX. A success message is displayed.
     * </p>
     *
     * @see IUserService#canDeleteUser(String, User)
     * @see IUserService#delete(User)
     */
    public void delete() {
        User currentUser = getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getId() : null;
        
        if (!userService.canDeleteUser(currentUserId, user)) {
            String errorMessage;
            if (currentUserId != null && currentUserId.equals(user.getId())) {
                errorMessage = "No puedes eliminar tu propio usuario";
            } else if (user.getRole() != null && user.getRole().equals(Role.ADMIN)) {
                errorMessage = "No se puede eliminar el último administrador del sistema";
            } else {
                errorMessage = "No se puede eliminar este usuario";
            }
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", errorMessage));
        } else {
            userService.delete(user);
            refreshUsers();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Usuario eliminado exitosamente"));
        }
        
        PrimeFaces.current().executeScript("PF('dlgDeleteUser').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:dt-users");
    }
}
