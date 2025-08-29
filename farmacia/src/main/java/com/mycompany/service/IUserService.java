/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.service;

import com.mycompany.model.entity.User;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author ramir
 */
@Local
public interface IUserService {
    User save(User user);
    User edit(User user);
    void delete(User user);
    List<User> list();    
}
