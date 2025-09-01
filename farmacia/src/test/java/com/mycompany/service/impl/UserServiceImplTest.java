/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.mycompany.service.impl;

import com.mycompany.model.entity.User;
import com.mycompany.repository.UserRepository;
import com.mycompany.util.PasswordUtils;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

/**
 *
 * @author ramir
 */
public class UserServiceImplTest {
    
    private UserServiceImpl service;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        service = new UserServiceImpl();
        userRepository = Mockito.mock(UserRepository.class);
        // Usa reflexión para inyectar el mock si no tienes setter
        java.lang.reflect.Field field = service.getClass().getDeclaredField("userRepository");
        field.setAccessible(true);
        field.set(service, userRepository);
    }

    @Test
    void testSave() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("plainPassword");
        
        // Mock findByUserName to return null, indicating the userName does not exist
        Mockito.when(userRepository.findByUserName(Mockito.anyString())).thenReturn(null);
        
        service.save(user);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).save(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertNotEquals("plainPassword", capturedUser.getPassword());
        assertTrue(PasswordUtils.checkPassword("plainPassword", capturedUser.getPassword()));
        assertEquals("johnD", capturedUser.getUserName()); // Corrected: Verify generated userName
    }

    @Test
    void testSaveWithExistingUserName() {
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setPassword("plainPassword");

        // Mock findByUserName to simulate existing userNames
        Mockito.when(userRepository.findByUserName("janeD")).thenReturn(new User()); // First attempt exists
        Mockito.when(userRepository.findByUserName("janeD1")).thenReturn(new User()); // Second attempt exists
        Mockito.when(userRepository.findByUserName("janeD2")).thenReturn(null); // Third attempt is unique
        
        service.save(user);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).save(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertEquals("janeD2", capturedUser.getUserName()); // Corrected: Verify generated unique userName
    }

    @Test
    void testEditWithPasswordChange() {
        User user = new User();
        user.setId("1");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("newPassword");

        User existingUser = new User();
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");

        Mockito.when(userRepository.findById("1")).thenReturn(existingUser);

        service.edit(user);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).update(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertNotEquals("newPassword", capturedUser.getPassword());
        assertTrue(PasswordUtils.checkPassword("newPassword", capturedUser.getPassword()));
    }
    
    @Test
    void testEditWithoutPasswordChange() {
        User user = new User();
        user.setId("1");
        user.setFirstName("John");
        user.setLastName("Doe");
        String hashedPassword = PasswordUtils.hashPassword("anypassword");
        user.setPassword(hashedPassword);

        User existingUser = new User();
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");

        Mockito.when(userRepository.findById("1")).thenReturn(existingUser);

        service.edit(user);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).update(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(hashedPassword, capturedUser.getPassword());
    }

    @Test
    void testDelete() {
        User user = new User();
        service.delete(user);
        Mockito.verify(userRepository).delete(user);
    }

    @Test
    void testList() {
        Mockito.when(userRepository.findAll()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), service.list());
    }

    @Test
    void testAuthenticateSuccess() {
        // Arrange
        String username = "testuser";
        String plainPassword = "password";
        String hashedPassword = PasswordUtils.hashPassword(plainPassword);
        
        User userFromRepo = new User();
        userFromRepo.setUserName(username);
        userFromRepo.setPassword(hashedPassword);
        
        when(userRepository.findByUserName(username)).thenReturn(userFromRepo);

        // Act
        User result = service.authenticate(username, plainPassword);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUserName());
    }

    @Test
    void testAuthenticateFailureWrongPassword() {
        // Arrange
        String username = "testuser";
        String correctPassword = "password";
        String wrongPassword = "wrongpassword";
        String hashedPassword = PasswordUtils.hashPassword(correctPassword);

        User userFromRepo = new User();
        userFromRepo.setUserName(username);
        userFromRepo.setPassword(hashedPassword);

        when(userRepository.findByUserName(username)).thenReturn(userFromRepo);

        // Act
        User result = service.authenticate(username, wrongPassword);

        // Assert
        assertNull(result);
    }

    @Test
    void testAuthenticateFailureUserNotFound() {
        // Arrange
        String username = "nonexistentuser";
        String password = "password";

        when(userRepository.findByUserName(username)).thenReturn(null);

        // Act
        User result = service.authenticate(username, password);

        // Assert
        assertNull(result);
    }
    
}
