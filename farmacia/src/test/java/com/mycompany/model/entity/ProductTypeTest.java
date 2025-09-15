package com.mycompany.model.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for ProductType entity
 * @author ramir
 */
public class ProductTypeTest {
    
    public ProductTypeTest() {
    }
    
    @Test
    @DisplayName("Must be able to create a ProductType instance")
    public void testCreateEmptyProductTypeInstance() {  
        ProductType productType = new ProductType();
        
        assertNotNull(productType);
    }
    
    @Test
    @DisplayName("ProductType class must have all required fields with correct types")
    void testProductTypeClassHasRequiredFields() throws NoSuchFieldException {
        Class<?> productTypeClass = ProductType.class;
        
        Field typeCodeField = productTypeClass.getDeclaredField("typeCode");
        assertEquals(String.class, typeCodeField.getType());
        
        Field typeNameField = productTypeClass.getDeclaredField("typeName");
        assertEquals(String.class, typeNameField.getType());
        
        Field descriptionField = productTypeClass.getDeclaredField("description");
        assertEquals(String.class, descriptionField.getType());
        
        Field isActiveField = productTypeClass.getDeclaredField("isActive");
        assertEquals(Boolean.class, isActiveField.getType());
        
        Field createdAtField = productTypeClass.getDeclaredField("createdAt");
        assertEquals(LocalDateTime.class, createdAtField.getType());
    }
    
    @Test
    @DisplayName("Parameterized constructor must have all required parameters")
    void testParameterizedConstructorHasAllParameters() throws NoSuchMethodException {
        Class<?> productTypeClass = ProductType.class;
        
        Constructor<?> constructor = productTypeClass.getDeclaredConstructor(
            String.class,      // typeCode
            String.class,      // typeName
            String.class,      // description
            Boolean.class,     // isActive
            LocalDateTime.class // createdAt
        );
        
        assertNotNull(constructor);
        assertEquals(5, constructor.getParameterCount());
    }

    @Test
    @DisplayName("ProductType class must have all basic methods (getters, setters, equals, hashCode, toString)")
    void testAllBasicMethodsExist() throws NoSuchMethodException {
        Class<?> productTypeClass = ProductType.class;
        
        // Getters
        assertNotNull(productTypeClass.getMethod("getTypeCode"));
        assertNotNull(productTypeClass.getMethod("getTypeName"));
        assertNotNull(productTypeClass.getMethod("getDescription"));
        assertNotNull(productTypeClass.getMethod("getIsActive"));
        assertNotNull(productTypeClass.getMethod("getCreatedAt"));
        
        // Setters
        assertNotNull(productTypeClass.getMethod("setTypeCode", String.class));
        assertNotNull(productTypeClass.getMethod("setTypeName", String.class));
        assertNotNull(productTypeClass.getMethod("setDescription", String.class));
        assertNotNull(productTypeClass.getMethod("setIsActive", Boolean.class));
        assertNotNull(productTypeClass.getMethod("setCreatedAt", LocalDateTime.class));
        
        // equals, hashCode, toString
        assertNotNull(productTypeClass.getMethod("equals", Object.class));
        assertNotNull(productTypeClass.getMethod("hashCode"));
        assertNotNull(productTypeClass.getMethod("toString"));
    }
    
    @Test
    @DisplayName("Builder pattern should work correctly")
    void testBuilderPattern() {
        ProductType productType = ProductType.builder()
                .typeCode("MED")
                .typeName("Medicine")
                .description("Medical products")
                .isActive(true)
                .build();
        
        assertNotNull(productType);
        assertEquals("MED", productType.getTypeCode());
        assertEquals("Medicine", productType.getTypeName());
        assertEquals("Medical products", productType.getDescription());
        assertTrue(productType.getIsActive());
    }
    
    @Test
    @DisplayName("Default values should be set correctly")
    void testDefaultValues() {
        ProductType productType = ProductType.builder()
                .typeCode("MED")
                .typeName("Medicine")
                .build();
        
        assertTrue(productType.getIsActive()); // Should default to true
    }
}