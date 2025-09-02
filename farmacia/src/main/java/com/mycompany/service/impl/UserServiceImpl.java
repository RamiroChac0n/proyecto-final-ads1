/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.service.impl;

import com.mycompany.model.entity.User;
import com.mycompany.model.entity.enums.Role;
import com.mycompany.repository.UserRepository;
import com.mycompany.service.IUserService;
import com.mycompany.util.PasswordUtils;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;
import java.util.logging.Logger; // Import Logger

/**
 *
 * @author ramir
 */
@Stateless
public class UserServiceImpl implements IUserService{
    
    private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName()); // Initialize Logger

    @EJB
    private UserRepository userRepository;

    @Override
    public User save(User user) {
        // Generate userName in the new format: firstName + first letter of lastName (capitalized)
        String baseUserName = user.getFirstName().toLowerCase() + 
                             Character.toUpperCase(user.getLastName().charAt(0));
        String uniqueUserName = baseUserName;
        int counter = 1;

        while (isUserNameExists(uniqueUserName)) {
            uniqueUserName = baseUserName + counter;
            counter++;
        }
        user.setUserName(uniqueUserName);

        // Hash password
        String hashedPassword = PasswordUtils.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);

        LOGGER.info("Saving user with userName: " + user.getUserName()); // Log userName
        return userRepository.save(user);
    }

    @Override
    public User edit(User user) {
        User existingUser = userRepository.findById(user.getId());

        // Regenerate username if names have changed
        if (!existingUser.getFirstName().equals(user.getFirstName()) || !existingUser.getLastName().equals(user.getLastName())) {
            String baseUserName = (user.getFirstName() + "." + user.getLastName()).toLowerCase();
            String uniqueUserName = baseUserName;
            int counter = 1;
            while (isUserNameExists(uniqueUserName)) {
                uniqueUserName = baseUserName + counter;
                counter++;
            }
            user.setUserName(uniqueUserName);
        }

        // Only hash the password if it's not already hashed
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            String hashedPassword = PasswordUtils.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);
        }
        return userRepository.update(user);
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }

    @Override
    public List<User> list() {
        return userRepository.findAll();
    }

    @Override
    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    public boolean isUserNameExists(String userName) {
        return userRepository.findByUserName(userName) != null;
    }

    @Override
    public User findById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public User authenticate(String username, String password) {
        User user = userRepository.findByUserName(username);
        if (user != null && PasswordUtils.checkPassword(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public long countAdminUsers() {
        return userRepository.countAdminUsers();
    }

    @Override
    public boolean canDeleteUser(String currentUserId, User userToDelete) {
        if (userToDelete == null) {
            return false;
        }
        
        if (currentUserId != null && currentUserId.equals(userToDelete.getId())) {
            return false;
        }
        
        if (Role.ADMIN.equals(userToDelete.getRole())) {
            long adminCount = countAdminUsers();
            if (adminCount <= 1) {
                return false;
            }
        }
        
        return true;
    }
    
}