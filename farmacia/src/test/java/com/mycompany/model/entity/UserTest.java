/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.mycompany.model.entity;

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
    public void mustCreateUserInstance() {
        // ARRANGE (Prepare)
        // We don't need to prepare anything special.
        
        // ACT (Act)
        User user = new User();
        
        // ASSERT (Verify)
        assertNotNull(user);
    }
    
}
