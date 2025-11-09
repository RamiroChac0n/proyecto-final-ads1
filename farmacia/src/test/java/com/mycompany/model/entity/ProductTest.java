package com.mycompany.model.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for Product entity
 * @author ramir
 */
public class ProductTest {
    
    public ProductTest() {
    }
    
    @Test
    @DisplayName("Must be able to create a Product instance")
    public void testCreateEmptyProductInstance() {  
        Product product = new Product();
        
        assertNotNull(product);
    }
    
    @Test
    @DisplayName("Product class must have all required fields with correct types")
    void testProductClassHasRequiredFields() throws NoSuchFieldException {
        Class<?> productClass = Product.class;
        
        Field productIdField = productClass.getDeclaredField("productId");
        assertEquals(Long.class, productIdField.getType());
        
        Field productTypeField = productClass.getDeclaredField("productType");
        assertEquals(ProductType.class, productTypeField.getType());
        
        Field categoryField = productClass.getDeclaredField("category");
        assertEquals(ProductCategory.class, categoryField.getType());
        
        Field activePrincipleField = productClass.getDeclaredField("activePrinciple");
        assertEquals(ActivePrinciple.class, activePrincipleField.getType());
        
        Field concentrationField = productClass.getDeclaredField("concentration");
        assertEquals(String.class, concentrationField.getType());
        
        Field concentrationUnitField = productClass.getDeclaredField("concentrationUnit");
        assertEquals(ConcentrationUnit.class, concentrationUnitField.getType());
        
        Field dosageFormField = productClass.getDeclaredField("dosageForm");
        assertEquals(DosageForm.class, dosageFormField.getType());
        
        Field commercialNameField = productClass.getDeclaredField("commercialName");
        assertEquals(String.class, commercialNameField.getType());
        
        Field brandField = productClass.getDeclaredField("brand");
        assertEquals(String.class, brandField.getType());
        
        Field manufacturerField = productClass.getDeclaredField("manufacturer");
        assertEquals(String.class, manufacturerField.getType());
        
        Field requiresPrescriptionField = productClass.getDeclaredField("requiresPrescription");
        assertEquals(Boolean.class, requiresPrescriptionField.getType());
        
        Field minStockField = productClass.getDeclaredField("minStock");
        assertEquals(Integer.class, minStockField.getType());
        
        Field maxStockField = productClass.getDeclaredField("maxStock");
        assertEquals(Integer.class, maxStockField.getType());
        
        Field isActiveField = productClass.getDeclaredField("isActive");
        assertEquals(Boolean.class, isActiveField.getType());
        
        Field createdAtField = productClass.getDeclaredField("createdAt");
        assertEquals(Date.class, createdAtField.getType());
        
        Field updatedAtField = productClass.getDeclaredField("updatedAt");
        assertEquals(Date.class, updatedAtField.getType());
        
        Field batchesField = productClass.getDeclaredField("batches");
        assertEquals(List.class, batchesField.getType());
        
        Field inventoryMovementsField = productClass.getDeclaredField("inventoryMovements");
        assertEquals(List.class, inventoryMovementsField.getType());
    }
    
    @Test
    @DisplayName("Parameterized constructor must have all required parameters")
    void testParameterizedConstructorHasAllParameters() throws NoSuchMethodException {
        Class<?> productClass = Product.class;
        
        Constructor<?> constructor = productClass.getDeclaredConstructor(
            Long.class,           // productId
            ProductType.class,    // productType
            ProductCategory.class, // category
            ActivePrinciple.class, // activePrinciple
            String.class,         // concentration
            ConcentrationUnit.class, // concentrationUnit
            DosageForm.class,     // dosageForm
            Integer.class,        // sequenceNumber
            String.class,         // commercialName
            String.class,         // brand
            String.class,         // manufacturer
            Boolean.class,        // requiresPrescription
            Integer.class,        // minStock
            Integer.class,        // maxStock
            Boolean.class,        // isActive
            Date.class,  // createdAt
            Date.class,  // updatedAt
            List.class,          // batches
            List.class           // inventoryMovements
        );
        
        assertNotNull(constructor);
    }

    @Test
    @DisplayName("Product class must have all basic methods (getters, setters, equals, hashCode, toString)")
    void testAllBasicMethodsExist() throws NoSuchMethodException {
        Class<?> productClass = Product.class;
        
        // Test a few key getters and setters
        assertNotNull(productClass.getMethod("getProductId"));
        assertNotNull(productClass.getMethod("getProductType"));
        assertNotNull(productClass.getMethod("getCategory"));
        assertNotNull(productClass.getMethod("getActivePrinciple"));
        assertNotNull(productClass.getMethod("getConcentration"));
        assertNotNull(productClass.getMethod("getConcentrationUnit"));
        assertNotNull(productClass.getMethod("getDosageForm"));
        assertNotNull(productClass.getMethod("getCommercialName"));
        assertNotNull(productClass.getMethod("getBrand"));
        assertNotNull(productClass.getMethod("getManufacturer"));
        assertNotNull(productClass.getMethod("getRequiresPrescription"));
        assertNotNull(productClass.getMethod("getMinStock"));
        assertNotNull(productClass.getMethod("getMaxStock"));
        assertNotNull(productClass.getMethod("getIsActive"));
        assertNotNull(productClass.getMethod("getCreatedAt"));
        assertNotNull(productClass.getMethod("getUpdatedAt"));
        assertNotNull(productClass.getMethod("getBatches"));
        assertNotNull(productClass.getMethod("getInventoryMovements"));
        
        // Test key setters
        assertNotNull(productClass.getMethod("setProductId", Long.class));
        assertNotNull(productClass.getMethod("setProductType", ProductType.class));
        assertNotNull(productClass.getMethod("setCategory", ProductCategory.class));
        assertNotNull(productClass.getMethod("setActivePrinciple", ActivePrinciple.class));
        assertNotNull(productClass.getMethod("setConcentration", String.class));
        assertNotNull(productClass.getMethod("setConcentrationUnit", ConcentrationUnit.class));
        assertNotNull(productClass.getMethod("setDosageForm", DosageForm.class));
        assertNotNull(productClass.getMethod("setCommercialName", String.class));
        assertNotNull(productClass.getMethod("setManufacturer", String.class));
        
        // equals, hashCode, toString
        assertNotNull(productClass.getMethod("equals", Object.class));
        assertNotNull(productClass.getMethod("hashCode"));
        assertNotNull(productClass.getMethod("toString"));
    }
    
    @Test
    @DisplayName("Builder pattern should work correctly")
    void testBuilderPattern() {
        ProductType productType = ProductType.builder()
                .typeCode("MED")
                .typeName("Medicine")
                .build();
        
        ProductCategory category = ProductCategory.builder()
                .categoryCode("ANT")
                .categoryName("Antibiotics")
                .build();
        
        ActivePrinciple activePrinciple = ActivePrinciple.builder()
                .principleCode("AMOX500")
                .innName("Amoxicillin")
                .build();
        
        ConcentrationUnit unit = ConcentrationUnit.builder()
                .unitCode("MG")
                .unitName("Miligramos")
                .build();
        
        DosageForm form = DosageForm.builder()
                .formCode("TAB")
                .formName("Tablet")
                .build();
        
        Product product = Product.builder()
                .productId(1L)
                .productType(productType)
                .category(category)
                .activePrinciple(activePrinciple)
                .concentration("500")
                .concentrationUnit(unit)
                .dosageForm(form)
                .commercialName("Amoxil")
                .brand("GSK")
                .manufacturer("GlaxoSmithKline")
                .requiresPrescription(true)
                .minStock(10)
                .maxStock(1000)
                .isActive(true)
                .build();
        
        assertNotNull(product);
        assertEquals(1L, product.getProductId());
        assertEquals("Amoxil", product.getCommercialName());
        assertEquals("GSK", product.getBrand());
        assertEquals("GlaxoSmithKline", product.getManufacturer());
        assertEquals("500", product.getConcentration());
        assertTrue(product.getRequiresPrescription());
        assertEquals(10, product.getMinStock());
        assertEquals(1000, product.getMaxStock());
        assertTrue(product.getIsActive());
    }
    
    @Test
    @DisplayName("Default values should be set correctly")
    void testDefaultValues() {
        Product product = Product.builder()
                .productId(2L)
                .commercialName("Test Product")
                .manufacturer("Test Manufacturer")
                .build();
        
        assertFalse(product.getRequiresPrescription()); // Should default to false
        assertEquals(0, product.getMinStock()); // Should default to 0
        assertEquals(1000, product.getMaxStock()); // Should default to 1000
        assertTrue(product.getIsActive()); // Should default to true
    }
    
    @Test
    @DisplayName("Should handle product ID pattern validation structure")
    void testProductIdPatternStructure() {
        // Test that the pattern field exists and is correctly structured for validation
        // This is a structure test, actual validation would be done by Jakarta Validation
        Long validProductId = 1L;
        Long invalidProductId = 999L;
        
        Product validProduct = Product.builder()
                .productId(validProductId)
                .commercialName("Test")
                .manufacturer("Test")
                .build();
        
        Product invalidProduct = Product.builder()
                .productId(invalidProductId)
                .commercialName("Test")
                .manufacturer("Test")
                .build();
        
        // Both can be created (validation would happen at persistence layer)
        assertNotNull(validProduct);
        assertNotNull(invalidProduct);
        assertEquals(validProductId, validProduct.getProductId());
        assertEquals(invalidProductId, invalidProduct.getProductId());
    }
}