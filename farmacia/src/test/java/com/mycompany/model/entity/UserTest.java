/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.mycompany.model.entity;

import com.mycompany.model.entity.enums.Role;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 *
 * @author ramir
 */
public class UserTest {
    
    public UserTest() {
    }
    
    @Test
    @DisplayName("Must be able to create a User instance")
    public void TestCreateEmptyUserInstance() {  
        User user = new User();
        
        assertNotNull(user);
    }
    
    @Test
    @DisplayName("User class must have all required fields with correct types")
    void testUserClassHasRequiredFields() throws NoSuchFieldException {
        Class<?> userClass = User.class;
        
        Field idField = userClass.getDeclaredField("id");
        assertEquals(String.class, idField.getType());
        
        Field firstNameField = userClass.getDeclaredField("firstName");
        assertEquals(String.class, firstNameField.getType());
        
        Field lastNameField = userClass.getDeclaredField("lastName");
        assertEquals(String.class, lastNameField.getType());
        
        Field userNameField = userClass.getDeclaredField("userName");
        assertEquals(String.class, userNameField.getType());
        
        Field phoneField = userClass.getDeclaredField("phoneNumber");
        assertEquals(String.class, phoneField.getType());
        
        Field emailField = userClass.getDeclaredField("email");
        assertEquals(String.class, emailField.getType());
        
        Field passwordField = userClass.getDeclaredField("password");
        assertEquals(String.class, passwordField.getType());
        
        Field roleField = userClass.getDeclaredField("role");
        assertEquals(Role.class, roleField.getType());
    }  
    
    @Test
    @DisplayName("Parameterized constructor must have all required parameters")
    void testParameterizedConstructorHasAllParameters() throws NoSuchMethodException {
        Class<?> userClass = User.class;
        
        Constructor<?> constructor = userClass.getDeclaredConstructor(
            String.class,  // id
            String.class,  // firstName
            String.class,  // lastName
            String.class,  // userName
            String.class,  // phoneNumber
            String.class,  // email
            String.class,  // password
            Role.class   // role
        );
        
        assertNotNull(constructor);
        
        assertEquals(8, constructor.getParameterCount()); // Updated parameter count
    }

    @Test
    @DisplayName("User class must have all basic methods (getters, setters, equals, hashCode, toString)")
    void testAllBasicMethodsExist() throws NoSuchMethodException {
        Class<?> userClass = User.class;
        
        // Getters
        assertNotNull(userClass.getMethod("getId"));
        assertNotNull(userClass.getMethod("getFirstName"));
        assertNotNull(userClass.getMethod("getLastName"));
        assertNotNull(userClass.getMethod("getUserName"));
        assertNotNull(userClass.getMethod("getPhoneNumber"));
        assertNotNull(userClass.getMethod("getEmail"));
        assertNotNull(userClass.getMethod("getPassword"));
        assertNotNull(userClass.getMethod("getRole"));
        
        // Setters
        assertNotNull(userClass.getMethod("setId", String.class));
        assertNotNull(userClass.getMethod("setFirstName", String.class));
        assertNotNull(userClass.getMethod("setLastName", String.class));
        assertNotNull(userClass.getMethod("setUserName", String.class));
        assertNotNull(userClass.getMethod("setPhoneNumber", String.class));
        assertNotNull(userClass.getMethod("setEmail", String.class));
        assertNotNull(userClass.getMethod("setPassword", String.class));
        assertNotNull(userClass.getMethod("setRole", Role.class));
        
        // equals, hashCode, toString
        assertNotNull(userClass.getMethod("equals", Object.class));
        assertNotNull(userClass.getMethod("hashCode"));
        assertNotNull(userClass.getMethod("toString"));
    }    
    
}
