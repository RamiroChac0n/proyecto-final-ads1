package com.mycompany.service;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.User;
import com.mycompany.model.entity.enums.Role;
import com.mycompany.service.IBranchService;
import com.mycompany.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminDataInitializerTest {

    @Mock
    private IUserService userService;

    @Mock
    private IBranchService branchService;

    @InjectMocks
    private AdminDataInitializer adminDataInitializer;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(userService, branchService);
    }
    
    @Test
    void testInitializeAdminUser_WhenNoUsersExist_CreatesAdminUser() {
        // Arrange
        when(userService.list()).thenReturn(new ArrayList<>());
        when(branchService.list()).thenReturn(new ArrayList<>());

        Branch mockBranch = Branch.builder()
                .branchId(1)
                .branchName("Casa Matriz")
                .address("Dirección Principal")
                .phone("00000000")
                .isActive(true)
                .build();
        when(branchService.save(any(Branch.class))).thenReturn(mockBranch);

        // Act
        adminDataInitializer.initializeAdminUser();

        // Assert
        verify(userService, times(1)).list();
        verify(branchService, times(1)).list();
        verify(branchService, times(1)).save(any(Branch.class));
        verify(userService, times(1)).save(argThat(user ->
            user != null &&
            "System".equals(user.getFirstName()) &&
            "Administrator".equals(user.getLastName()) &&
            "".equals(user.getUserName()) &&
            "admin@pharmacy.com".equals(user.getEmail()) &&
            "root".equals(user.getPassword()) &&
            "12345678".equals(user.getPhoneNumber()) &&
            Role.ADMIN.equals(user.getRole()) &&
            user.getBranch() != null &&
            user.getId() != null &&
            user.getId().length() >= 13
        ));
    }
    
    @Test
    void testInitializeAdminUser_WhenAdminUserExists_DoesNotCreateAnotherOne() {
        // Arrange
        List<User> existingUsers = new ArrayList<>();
        User existingAdmin = User.builder()
                .id("ADMIN12345678")
                .firstName("Existing")
                .lastName("Admin")
                .userName("existingadmin")
                .email("existing@pharmacy.com")
                .password("hashedpassword")
                .role(Role.ADMIN)
                .build();
        existingUsers.add(existingAdmin);
        
        when(userService.list()).thenReturn(existingUsers);
        
        // Act
        adminDataInitializer.initializeAdminUser();
        
        // Assert
        verify(userService, times(1)).list();
        verify(userService, never()).save(any(User.class));
    }
    
    @Test
    void testInitializeAdminUser_WhenOnlyNonAdminUsersExist_CreatesAdminUser() {
        // Arrange
        List<User> existingUsers = new ArrayList<>();
        User cashier = User.builder()
                .id("CASHIER123456")
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .email("john@pharmacy.com")
                .password("hashedpassword")
                .role(Role.CASHIER)
                .build();
        existingUsers.add(cashier);

        when(userService.list()).thenReturn(existingUsers);
        when(branchService.list()).thenReturn(new ArrayList<>());

        Branch mockBranch = Branch.builder()
                .branchId(1)
                .branchName("Casa Matriz")
                .address("Dirección Principal")
                .phone("00000000")
                .isActive(true)
                .build();
        when(branchService.save(any(Branch.class))).thenReturn(mockBranch);

        // Act
        adminDataInitializer.initializeAdminUser();

        // Assert
        verify(userService, times(1)).list();
        verify(branchService, times(1)).list();
        verify(branchService, times(1)).save(any(Branch.class));
        verify(userService, times(1)).save(argThat(user ->
            user != null &&
            Role.ADMIN.equals(user.getRole()) &&
            user.getBranch() != null
        ));
    }
    
    @Test
    void testInitializeAdminUser_WhenServiceThrowsException_HandlesGracefully() {
        // Arrange
        when(userService.list()).thenThrow(new RuntimeException("Database error"));
        
        // Act (should not throw exception)
        adminDataInitializer.initializeAdminUser();
        
        // Assert
        verify(userService, times(1)).list();
        verify(userService, never()).save(any(User.class));
    }
}