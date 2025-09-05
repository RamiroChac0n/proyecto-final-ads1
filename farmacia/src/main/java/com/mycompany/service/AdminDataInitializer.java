package com.mycompany.service;

import com.mycompany.model.entity.User;
import com.mycompany.model.entity.enums.Role;
import com.mycompany.service.IUserService;
import com.mycompany.util.PasswordUtils;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.util.List;
import java.util.logging.Logger;
import java.util.UUID;

@Singleton
@Startup
public class AdminDataInitializer {
    
    private static final Logger LOGGER = Logger.getLogger(AdminDataInitializer.class.getName());
    
    @EJB
    private IUserService userService;
    
    @PostConstruct
    public void initializeAdminUser() {
        LOGGER.info("Checking if admin user initialization is needed...");
        
        try {
            List<User> users = userService.list();
            boolean hasAdminUser = users.stream()
                    .anyMatch(user -> Role.ADMIN.equals(user.getRole()));
            
            if (!hasAdminUser) {
                LOGGER.info("No admin user found. Creating default admin user...");
                createDefaultAdminUser();
                LOGGER.info("Default admin user created successfully.");
            } else {
                LOGGER.info("Admin user already exists. Skipping initialization.");
            }
        } catch (Exception e) {
            LOGGER.severe("Error during admin user initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createDefaultAdminUser() {
        String adminId = generateUserId();
        
        User adminUser = User.builder()
                .id(adminId)
                .firstName("System")
                .lastName("Administrator")
                .userName("")
                .email("admin@pharmacy.com")
                .password("root")
                .phoneNumber("12345678")
                .role(Role.ADMIN)
                .build();
        
        userService.save(adminUser);
        LOGGER.info("Admin user created with username: " + adminUser.getUserName());
    }
    
    private String generateUserId() {
        return "ADM" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}