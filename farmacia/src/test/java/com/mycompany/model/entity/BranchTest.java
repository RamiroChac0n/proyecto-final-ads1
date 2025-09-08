package com.mycompany.model.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for Branch entity
 * @author ramir
 */
public class BranchTest {
    
    public BranchTest() {
    }
    
    @Test
    @DisplayName("Must be able to create a Branch instance")
    public void testCreateEmptyBranchInstance() {  
        Branch branch = new Branch();
        
        assertNotNull(branch);
    }
    
    @Test
    @DisplayName("Branch class must have all required fields with correct types")
    void testBranchClassHasRequiredFields() throws NoSuchFieldException {
        Class<?> branchClass = Branch.class;
        
        Field branchIdField = branchClass.getDeclaredField("branchId");
        assertEquals(Integer.class, branchIdField.getType());
        
        Field branchNameField = branchClass.getDeclaredField("branchName");
        assertEquals(String.class, branchNameField.getType());
        
        Field addressField = branchClass.getDeclaredField("address");
        assertEquals(String.class, addressField.getType());
        
        Field phoneField = branchClass.getDeclaredField("phone");
        assertEquals(String.class, phoneField.getType());
        
        Field isActiveField = branchClass.getDeclaredField("isActive");
        assertEquals(Boolean.class, isActiveField.getType());
        
        Field inventoryMovementsField = branchClass.getDeclaredField("inventoryMovements");
        assertEquals(List.class, inventoryMovementsField.getType());
    }
    
    @Test
    @DisplayName("Parameterized constructor must have all required parameters")
    void testParameterizedConstructorHasAllParameters() throws NoSuchMethodException {
        Class<?> branchClass = Branch.class;
        
        Constructor<?> constructor = branchClass.getDeclaredConstructor(
            Integer.class, // branchId
            String.class,  // branchName
            String.class,  // address
            String.class,  // phone
            Boolean.class, // isActive
            List.class     // inventoryMovements
        );
        
        assertNotNull(constructor);
        assertEquals(6, constructor.getParameterCount());
    }

    @Test
    @DisplayName("Branch class must have all basic methods (getters, setters, equals, hashCode, toString)")
    void testAllBasicMethodsExist() throws NoSuchMethodException {
        Class<?> branchClass = Branch.class;
        
        // Getters
        assertNotNull(branchClass.getMethod("getBranchId"));
        assertNotNull(branchClass.getMethod("getBranchName"));
        assertNotNull(branchClass.getMethod("getAddress"));
        assertNotNull(branchClass.getMethod("getPhone"));
        assertNotNull(branchClass.getMethod("getIsActive"));
        assertNotNull(branchClass.getMethod("getInventoryMovements"));
        
        // Setters
        assertNotNull(branchClass.getMethod("setBranchId", Integer.class));
        assertNotNull(branchClass.getMethod("setBranchName", String.class));
        assertNotNull(branchClass.getMethod("setAddress", String.class));
        assertNotNull(branchClass.getMethod("setPhone", String.class));
        assertNotNull(branchClass.getMethod("setIsActive", Boolean.class));
        assertNotNull(branchClass.getMethod("setInventoryMovements", List.class));
        
        // equals, hashCode, toString
        assertNotNull(branchClass.getMethod("equals", Object.class));
        assertNotNull(branchClass.getMethod("hashCode"));
        assertNotNull(branchClass.getMethod("toString"));
    }
    
    @Test
    @DisplayName("Builder pattern should work correctly")
    void testBuilderPattern() {
        Branch branch = Branch.builder()
                .branchId(1)
                .branchName("Farmacia Central")
                .address("Av. Principal #123, Zona 1")
                .phone("2234-5678")
                .isActive(true)
                .build();
        
        assertNotNull(branch);
        assertEquals(1, branch.getBranchId());
        assertEquals("Farmacia Central", branch.getBranchName());
        assertEquals("Av. Principal #123, Zona 1", branch.getAddress());
        assertEquals("2234-5678", branch.getPhone());
        assertTrue(branch.getIsActive());
    }
    
    @Test
    @DisplayName("Default values should be set correctly")
    void testDefaultValues() {
        Branch branch = Branch.builder()
                .branchName("Farmacia Norte")
                .build();
        
        assertTrue(branch.getIsActive()); // Should default to true
    }
    
    @Test
    @DisplayName("Should handle optional fields correctly")
    void testOptionalFields() {
        // Test branch with all fields
        Branch fullBranch = Branch.builder()
                .branchId(2)
                .branchName("Farmacia Sur")
                .address("Calle 15, Zona 10")
                .phone("2345-6789")
                .isActive(true)
                .build();
        
        assertNotNull(fullBranch.getAddress());
        assertNotNull(fullBranch.getPhone());
        
        // Test branch with only required fields
        Branch minimalBranch = Branch.builder()
                .branchId(3)
                .branchName("Farmacia Este")
                .build();
        
        assertEquals("Farmacia Este", minimalBranch.getBranchName());
        assertNull(minimalBranch.getAddress()); // Optional field can be null
        assertNull(minimalBranch.getPhone()); // Optional field can be null
        assertTrue(minimalBranch.getIsActive()); // Default value
    }
    
    @Test
    @DisplayName("Should handle different types of branch information")
    void testDifferentBranchTypes() {
        Branch mainBranch = Branch.builder()
                .branchId(1)
                .branchName("Farmacia Matriz")
                .address("Centro Comercial Plaza Mayor, Local 15")
                .phone("2234-5678")
                .isActive(true)
                .build();
        
        Branch smallBranch = Branch.builder()
                .branchId(2)
                .branchName("Farmacia Express")
                .address("Hospital Nacional, Piso 1")
                .phone("2345-6789")
                .isActive(true)
                .build();
        
        Branch inactiveBranch = Branch.builder()
                .branchId(3)
                .branchName("Farmacia Antigua")
                .address("Calle Vieja #45")
                .phone("2456-7890")
                .isActive(false) // Inactive branch
                .build();
        
        assertTrue(mainBranch.getIsActive());
        assertTrue(smallBranch.getIsActive());
        assertFalse(inactiveBranch.getIsActive());
        
        assertEquals("Farmacia Matriz", mainBranch.getBranchName());
        assertEquals("Farmacia Express", smallBranch.getBranchName());
        assertEquals("Farmacia Antigua", inactiveBranch.getBranchName());
    }
}