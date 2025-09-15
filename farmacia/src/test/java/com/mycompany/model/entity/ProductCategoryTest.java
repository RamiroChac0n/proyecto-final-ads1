package com.mycompany.model.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for ProductCategory entity
 * @author ramir
 */
public class ProductCategoryTest {
    
    public ProductCategoryTest() {
    }
    
    @Test
    @DisplayName("Must be able to create a ProductCategory instance")
    public void testCreateEmptyProductCategoryInstance() {  
        ProductCategory productCategory = new ProductCategory();
        
        assertNotNull(productCategory);
    }
    
    @Test
    @DisplayName("ProductCategory class must have all required fields with correct types")
    void testProductCategoryClassHasRequiredFields() throws NoSuchFieldException {
        Class<?> productCategoryClass = ProductCategory.class;
        
        Field categoryCodeField = productCategoryClass.getDeclaredField("categoryCode");
        assertEquals(String.class, categoryCodeField.getType());
        
        Field categoryNameField = productCategoryClass.getDeclaredField("categoryName");
        assertEquals(String.class, categoryNameField.getType());
        
        Field descriptionField = productCategoryClass.getDeclaredField("description");
        assertEquals(String.class, descriptionField.getType());
        
        Field requiresPrescriptionField = productCategoryClass.getDeclaredField("requiresPrescription");
        assertEquals(Boolean.class, requiresPrescriptionField.getType());
        
        Field isActiveField = productCategoryClass.getDeclaredField("isActive");
        assertEquals(Boolean.class, isActiveField.getType());
        
        Field createdAtField = productCategoryClass.getDeclaredField("createdAt");
        assertEquals(LocalDateTime.class, createdAtField.getType());
    }
    
    @Test
    @DisplayName("Parameterized constructor must have all required parameters")
    void testParameterizedConstructorHasAllParameters() throws NoSuchMethodException {
        Class<?> productCategoryClass = ProductCategory.class;
        
        Constructor<?> constructor = productCategoryClass.getDeclaredConstructor(
            String.class,       // categoryCode
            String.class,       // categoryName
            String.class,       // description
            Boolean.class,      // requiresPrescription
            Boolean.class,      // isActive
            LocalDateTime.class // createdAt
        );
        
        assertNotNull(constructor);
        assertEquals(6, constructor.getParameterCount());
    }

    @Test
    @DisplayName("ProductCategory class must have all basic methods (getters, setters, equals, hashCode, toString)")
    void testAllBasicMethodsExist() throws NoSuchMethodException {
        Class<?> productCategoryClass = ProductCategory.class;
        
        // Getters
        assertNotNull(productCategoryClass.getMethod("getCategoryCode"));
        assertNotNull(productCategoryClass.getMethod("getCategoryName"));
        assertNotNull(productCategoryClass.getMethod("getDescription"));
        assertNotNull(productCategoryClass.getMethod("getRequiresPrescription"));
        assertNotNull(productCategoryClass.getMethod("getIsActive"));
        assertNotNull(productCategoryClass.getMethod("getCreatedAt"));
        
        // Setters
        assertNotNull(productCategoryClass.getMethod("setCategoryCode", String.class));
        assertNotNull(productCategoryClass.getMethod("setCategoryName", String.class));
        assertNotNull(productCategoryClass.getMethod("setDescription", String.class));
        assertNotNull(productCategoryClass.getMethod("setRequiresPrescription", Boolean.class));
        assertNotNull(productCategoryClass.getMethod("setIsActive", Boolean.class));
        assertNotNull(productCategoryClass.getMethod("setCreatedAt", LocalDateTime.class));
        
        // equals, hashCode, toString
        assertNotNull(productCategoryClass.getMethod("equals", Object.class));
        assertNotNull(productCategoryClass.getMethod("hashCode"));
        assertNotNull(productCategoryClass.getMethod("toString"));
    }
    
    @Test
    @DisplayName("Builder pattern should work correctly")
    void testBuilderPattern() {
        ProductCategory productCategory = ProductCategory.builder()
                .categoryCode("ANT")
                .categoryName("Antibiotics")
                .description("Antibiotic medications")
                .requiresPrescription(true)
                .isActive(true)
                .build();
        
        assertNotNull(productCategory);
        assertEquals("ANT", productCategory.getCategoryCode());
        assertEquals("Antibiotics", productCategory.getCategoryName());
        assertEquals("Antibiotic medications", productCategory.getDescription());
        assertTrue(productCategory.getRequiresPrescription());
        assertTrue(productCategory.getIsActive());
    }
    
    @Test
    @DisplayName("Default values should be set correctly")
    void testDefaultValues() {
        ProductCategory productCategory = ProductCategory.builder()
                .categoryCode("VIT")
                .categoryName("Vitamins")
                .build();
        
        assertFalse(productCategory.getRequiresPrescription()); // Should default to false
        assertTrue(productCategory.getIsActive()); // Should default to true
    }
}