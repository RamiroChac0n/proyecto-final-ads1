/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.controller;

import com.mycompany.model.entity.User;
import com.mycompany.service.IUserService;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
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
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Error", "Invalid credentials"));
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

    // User management methods
    public void createNew() {
        user = new User();
    }

    public List<User> getUsers() {
        return users = userService.list();
    }

    public void save() {
        User existing = userService.findById(user.getId());
        if (existing == null) {
            userService.save(user);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("User added"));
        } else {
            userService.edit(user);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("User edited"));
        }
        createNew();
        PrimeFaces.current().executeScript("PF('dlgUserRegister').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:dt-users");
    }

    public void delete() {
        userService.delete(user);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("User deleted"));
        PrimeFaces.current().executeScript("PF('dlgDeleteUser').hide()");
        PrimeFaces.current().ajax().update("form:messages", "form:dt-users");
    }
}
