/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.controller;

import com.mycompany.model.entity.User;
import com.mycompany.service.IUserService;
import jakarta.ejb.EJB;
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
@Named(value = "userMB")
@ViewScoped
public class UserController implements Serializable{
    @EJB
    private IUserService userService;
    private User user;
    private List<User> users;  
    
    public void createNew(){
        user = new User();
    }    
    
    public List<User> getUsers(){
        return users = userService.list();
    }   
    
    public void save(){
        if(user.getId() == null){
            userService.save(user);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("User added"));
        }
        else{
            userService.edit(user);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("User edited"));
        }
        createNew();
        PrimeFaces.current().executeScript("PF('dlgUserRegister').hide()");
        PrimeFaces.current().ajax().update("form:messages","form:dt-users");
    }    
    
    public void delete(){
        userService.delete(user);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("User deleted"));
        PrimeFaces.current().executeScript("PF('dlgDeleteUser').hide()");
        PrimeFaces.current().ajax().update("form:messages","form:dt-users");        
    }    
}
