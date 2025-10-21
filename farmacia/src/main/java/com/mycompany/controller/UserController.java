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
 *
 * @author ramir
 */
@Data
@Named(value = "userController")
@SessionScoped
public class UserController implements Serializable {

    @EJB
    private IUserService userService;

    // Login fields
    private String username;
    private String password;
    
    // User management fields
    private User user;
    private List<User> users;

    // Login action
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

    // Logout action
    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        user = null;
        return "login?faces-redirect=true";
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user") != null;
    }
    
    // Page access check
    public void checkLogin() {
        if (!isLoggedIn()) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // Get current logged-in user
    public User getCurrentUser() {
        return (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
    }
    
    // Check if current user is admin
    public boolean isAdmin() {
        User currentUser = getCurrentUser();
        return currentUser != null && Role.ADMIN.equals(currentUser.getRole());
    }

    // Check if current user can process sales (ADMIN, CASHIER, or STOREKEEPER)
    public boolean canProcessSales() {
        User currentUser = getCurrentUser();
        return currentUser != null &&
               (Role.ADMIN.equals(currentUser.getRole()) ||
                Role.CASHIER.equals(currentUser.getRole()) ||
                Role.STOREKEEPER.equals(currentUser.getRole()));
    }

    // Check if current user is admin or cashier
    public boolean isAdminOrCashier() {
        User currentUser = getCurrentUser();
        return currentUser != null &&
               (Role.ADMIN.equals(currentUser.getRole()) ||
                Role.CASHIER.equals(currentUser.getRole()));
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

    // User management methods
    @PostConstruct
    public void init() {
        refreshUsers();
    }
    
    public void refreshUsers() {
        users = userService.list();
    }
    
    public void createNew() {
        user = new User();
    }

    public List<User> getUsers() {
        if (users == null) {
            users = userService.list();
        }
        return users;
    }

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
