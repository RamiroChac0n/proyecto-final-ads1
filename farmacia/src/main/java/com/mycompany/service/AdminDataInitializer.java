package com.mycompany.service;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.User;
import com.mycompany.model.entity.enums.Role;
import com.mycompany.service.IBranchService;
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

    @EJB
    private IBranchService branchService;
    
    @PostConstruct
    public void initializeAdminUser() {
        LOGGER.info("Checking if admin user initialization is needed...");

        try {
            List<User> users = userService.list();
            boolean hasAdminUser = users.stream()
                    .anyMatch(user -> Role.ADMIN.equals(user.getRole()));

            if (!hasAdminUser) {
                LOGGER.info("No admin user found. Creating default branch and admin user...");
                Branch defaultBranch = createDefaultBranch();
                createDefaultAdminUser(defaultBranch);
                LOGGER.info("Default branch and admin user created successfully.");
            } else {
                LOGGER.info("Admin user already exists. Skipping initialization.");
            }
        } catch (Exception e) {
            LOGGER.severe("Error during admin user initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Branch createDefaultBranch() {
        // Check if default branch already exists
        List<Branch> branches = branchService.list();
        if (!branches.isEmpty()) {
            LOGGER.info("Branches already exist. Using first branch as default.");
            return branches.get(0);
        }

        // Create new default branch
        Branch defaultBranch = Branch.builder()
                .branchName("Casa Matriz")
                .address("Dirección Principal")
                .phone("00000000")
                .isActive(true)
                .build();

        defaultBranch = branchService.save(defaultBranch);
        LOGGER.info("Default branch 'Casa Matriz' created with ID: " + defaultBranch.getBranchId());
        return defaultBranch;
    }

    private void createDefaultAdminUser(Branch branch) {
        String adminId = generateUserId();

        User adminUser = User.builder()
                .id(adminId)
                .firstName("System")
                .lastName("Administrator")
                .userName("")
                .email("admin@pharmacy.com")
                .password("root")
                .phoneNumber("12345678")
                .branch(branch)
                .role(Role.ADMIN)
                .build();

        userService.save(adminUser);
        LOGGER.info("Admin user created with username: " + adminUser.getUserName() + " assigned to branch: " + branch.getBranchName());
    }
    
    private String generateUserId() {
        return "ADM" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}