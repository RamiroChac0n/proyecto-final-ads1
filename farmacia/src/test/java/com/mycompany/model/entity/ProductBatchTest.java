package com.mycompany.model.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for ProductBatch entity
 * @author ramir
 */
public class ProductBatchTest {
    
    public ProductBatchTest() {
    }
    
    @Test
    @DisplayName("Must be able to create a ProductBatch instance")
    public void testCreateEmptyProductBatchInstance() {  
        ProductBatch productBatch = new ProductBatch();
        
        assertNotNull(productBatch);
    }
    
    @Test
    @DisplayName("ProductBatch class must have all required fields with correct types")
    void testProductBatchClassHasRequiredFields() throws NoSuchFieldException {
        Class<?> productBatchClass = ProductBatch.class;
        
        Field batchIdField = productBatchClass.getDeclaredField("batchId");
        assertEquals(Integer.class, batchIdField.getType());
        
        Field productField = productBatchClass.getDeclaredField("product");
        assertEquals(Product.class, productField.getType());
        
        Field batchNumberField = productBatchClass.getDeclaredField("batchNumber");
        assertEquals(String.class, batchNumberField.getType());
        
        Field quantityReceivedField = productBatchClass.getDeclaredField("quantityReceived");
        assertEquals(Integer.class, quantityReceivedField.getType());
        
        Field quantityAvailableField = productBatchClass.getDeclaredField("quantityAvailable");
        assertEquals(Integer.class, quantityAvailableField.getType());
        
        Field unitCostField = productBatchClass.getDeclaredField("unitCost");
        assertEquals(BigDecimal.class, unitCostField.getType());
        
        Field salePriceField = productBatchClass.getDeclaredField("salePrice");
        assertEquals(BigDecimal.class, salePriceField.getType());
        
        Field manufactureDateField = productBatchClass.getDeclaredField("manufactureDate");
        assertEquals(LocalDate.class, manufactureDateField.getType());
        
        Field expirationDateField = productBatchClass.getDeclaredField("expirationDate");
        assertEquals(LocalDate.class, expirationDateField.getType());
        
        Field receivedDateField = productBatchClass.getDeclaredField("receivedDate");
        assertEquals(LocalDate.class, receivedDateField.getType());
        
        Field isActiveField = productBatchClass.getDeclaredField("isActive");
        assertEquals(Boolean.class, isActiveField.getType());
        
        Field isExpiredField = productBatchClass.getDeclaredField("isExpired");
        assertEquals(Boolean.class, isExpiredField.getType());
        
        Field daysUntilExpirationField = productBatchClass.getDeclaredField("daysUntilExpiration");
        assertEquals(Integer.class, daysUntilExpirationField.getType());
        
        Field createdAtField = productBatchClass.getDeclaredField("createdAt");
        assertEquals(LocalDateTime.class, createdAtField.getType());
        
        Field inventoryMovementsField = productBatchClass.getDeclaredField("inventoryMovements");
        assertEquals(List.class, inventoryMovementsField.getType());
    }
    
    @Test
    @DisplayName("Parameterized constructor must have all required parameters")
    void testParameterizedConstructorHasAllParameters() throws NoSuchMethodException {
        Class<?> productBatchClass = ProductBatch.class;
        
        Constructor<?> constructor = productBatchClass.getDeclaredConstructor(
            Integer.class,      // batchId
            Product.class,      // product
            String.class,       // batchNumber
            Integer.class,      // quantityReceived
            Integer.class,      // quantityAvailable
            BigDecimal.class,   // unitCost
            BigDecimal.class,   // salePrice
            LocalDate.class,    // manufactureDate
            LocalDate.class,    // expirationDate
            LocalDate.class,    // receivedDate
            Boolean.class,      // isActive
            Boolean.class,      // isExpired
            Integer.class,      // daysUntilExpiration
            LocalDateTime.class, // createdAt
            List.class          // inventoryMovements
        );
        
        assertNotNull(constructor);
        assertEquals(15, constructor.getParameterCount());
    }

    @Test
    @DisplayName("ProductBatch class must have all basic methods (getters, setters, equals, hashCode, toString)")
    void testAllBasicMethodsExist() throws NoSuchMethodException {
        Class<?> productBatchClass = ProductBatch.class;
        
        // Test key getters
        assertNotNull(productBatchClass.getMethod("getBatchId"));
        assertNotNull(productBatchClass.getMethod("getProduct"));
        assertNotNull(productBatchClass.getMethod("getBatchNumber"));
        assertNotNull(productBatchClass.getMethod("getQuantityReceived"));
        assertNotNull(productBatchClass.getMethod("getQuantityAvailable"));
        assertNotNull(productBatchClass.getMethod("getUnitCost"));
        assertNotNull(productBatchClass.getMethod("getSalePrice"));
        assertNotNull(productBatchClass.getMethod("getManufactureDate"));
        assertNotNull(productBatchClass.getMethod("getExpirationDate"));
        assertNotNull(productBatchClass.getMethod("getReceivedDate"));
        assertNotNull(productBatchClass.getMethod("getIsActive"));
        assertNotNull(productBatchClass.getMethod("getIsExpired"));
        assertNotNull(productBatchClass.getMethod("getDaysUntilExpiration"));
        assertNotNull(productBatchClass.getMethod("getCreatedAt"));
        assertNotNull(productBatchClass.getMethod("getInventoryMovements"));
        
        // Test key setters
        assertNotNull(productBatchClass.getMethod("setBatchId", Integer.class));
        assertNotNull(productBatchClass.getMethod("setProduct", Product.class));
        assertNotNull(productBatchClass.getMethod("setBatchNumber", String.class));
        assertNotNull(productBatchClass.getMethod("setQuantityReceived", Integer.class));
        assertNotNull(productBatchClass.getMethod("setQuantityAvailable", Integer.class));
        assertNotNull(productBatchClass.getMethod("setUnitCost", BigDecimal.class));
        assertNotNull(productBatchClass.getMethod("setSalePrice", BigDecimal.class));
        assertNotNull(productBatchClass.getMethod("setExpirationDate", LocalDate.class));
        
        // equals, hashCode, toString
        assertNotNull(productBatchClass.getMethod("equals", Object.class));
        assertNotNull(productBatchClass.getMethod("hashCode"));
        assertNotNull(productBatchClass.getMethod("toString"));
    }
    
    @Test
    @DisplayName("Builder pattern should work correctly with BigDecimal and LocalDate")
    void testBuilderPattern() {
        Product product = Product.builder()
                .productId(1L)
                .commercialName("Test Product")
                .manufacturer("Test Manufacturer")
                .build();
        
        ProductBatch productBatch = ProductBatch.builder()
                .product(product)
                .batchNumber("BATCH001")
                .quantityReceived(100)
                .quantityAvailable(100)
                .unitCost(new BigDecimal("10.50"))
                .salePrice(new BigDecimal("15.75"))
                .manufactureDate(LocalDate.of(2024, 1, 15))
                .expirationDate(LocalDate.of(2026, 1, 15))
                .receivedDate(LocalDate.of(2024, 2, 1))
                .isActive(true)
                .isExpired(false)
                .daysUntilExpiration(365)
                .build();
        
        assertNotNull(productBatch);
        assertEquals("BATCH001", productBatch.getBatchNumber());
        assertEquals(100, productBatch.getQuantityReceived());
        assertEquals(100, productBatch.getQuantityAvailable());
        assertEquals(new BigDecimal("10.50"), productBatch.getUnitCost());
        assertEquals(new BigDecimal("15.75"), productBatch.getSalePrice());
        assertEquals(LocalDate.of(2024, 1, 15), productBatch.getManufactureDate());
        assertEquals(LocalDate.of(2026, 1, 15), productBatch.getExpirationDate());
        assertEquals(LocalDate.of(2024, 2, 1), productBatch.getReceivedDate());
        assertTrue(productBatch.getIsActive());
        assertFalse(productBatch.getIsExpired());
        assertEquals(365, productBatch.getDaysUntilExpiration());
    }
    
    @Test
    @DisplayName("Default values should be set correctly")
    void testDefaultValues() {
        ProductBatch productBatch = ProductBatch.builder()
                .batchNumber("BATCH002")
                .quantityReceived(50)
                .quantityAvailable(50)
                .unitCost(new BigDecimal("5.00"))
                .salePrice(new BigDecimal("7.50"))
                .expirationDate(LocalDate.of(2025, 12, 31))
                .build();
        
        assertTrue(productBatch.getIsActive()); // Should default to true
        assertFalse(productBatch.getIsExpired()); // Should default to false
        assertEquals(0, productBatch.getDaysUntilExpiration()); // Should default to 0
    }
    
    @Test
    @DisplayName("Should handle BigDecimal precision correctly")
    void testBigDecimalPrecision() {
        ProductBatch productBatch = ProductBatch.builder()
                .batchNumber("PRECISION-TEST")
                .quantityReceived(1)
                .quantityAvailable(1)
                .unitCost(new BigDecimal("99.99"))
                .salePrice(new BigDecimal("149.95"))
                .expirationDate(LocalDate.of(2025, 6, 30))
                .build();
        
        // Verify precision is maintained
        assertEquals(new BigDecimal("99.99"), productBatch.getUnitCost());
        assertEquals(new BigDecimal("149.95"), productBatch.getSalePrice());
        
        // Test with more decimal places
        productBatch.setUnitCost(new BigDecimal("12.345"));
        productBatch.setSalePrice(new BigDecimal("18.678"));
        
        assertEquals(new BigDecimal("12.345"), productBatch.getUnitCost());
        assertEquals(new BigDecimal("18.678"), productBatch.getSalePrice());
    }
    
    @Test
    @DisplayName("Should handle date fields correctly")
    void testDateFieldsHandling() {
        LocalDate manufactureDate = LocalDate.of(2024, 3, 15);
        LocalDate expirationDate = LocalDate.of(2027, 3, 15);
        LocalDate receivedDate = LocalDate.of(2024, 4, 1);
        
        ProductBatch productBatch = ProductBatch.builder()
                .batchNumber("DATE-TEST")
                .quantityReceived(25)
                .quantityAvailable(25)
                .unitCost(new BigDecimal("8.00"))
                .salePrice(new BigDecimal("12.00"))
                .manufactureDate(manufactureDate)
                .expirationDate(expirationDate)
                .receivedDate(receivedDate)
                .build();
        
        assertEquals(manufactureDate, productBatch.getManufactureDate());
        assertEquals(expirationDate, productBatch.getExpirationDate());
        assertEquals(receivedDate, productBatch.getReceivedDate());
        
        // Verify that dates can be modified independently
        LocalDate newManufactureDate = LocalDate.of(2024, 5, 20);
        productBatch.setManufactureDate(newManufactureDate);
        assertEquals(newManufactureDate, productBatch.getManufactureDate());
        // Original variable should remain unchanged
        assertEquals(LocalDate.of(2024, 3, 15), manufactureDate);
    }
}