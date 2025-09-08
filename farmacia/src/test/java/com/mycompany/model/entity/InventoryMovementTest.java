package com.mycompany.model.entity;

import com.mycompany.model.entity.enums.MovementType;
import com.mycompany.model.entity.enums.Role;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for InventoryMovement entity
 * @author ramir
 */
public class InventoryMovementTest {
    
    public InventoryMovementTest() {
    }
    
    @Test
    @DisplayName("Must be able to create an InventoryMovement instance")
    public void testCreateEmptyInventoryMovementInstance() {  
        InventoryMovement inventoryMovement = new InventoryMovement();
        
        assertNotNull(inventoryMovement);
    }
    
    @Test
    @DisplayName("InventoryMovement class must have all required fields with correct types")
    void testInventoryMovementClassHasRequiredFields() throws NoSuchFieldException {
        Class<?> inventoryMovementClass = InventoryMovement.class;
        
        Field movementIdField = inventoryMovementClass.getDeclaredField("movementId");
        assertEquals(Integer.class, movementIdField.getType());
        
        Field productField = inventoryMovementClass.getDeclaredField("product");
        assertEquals(Product.class, productField.getType());
        
        Field batchField = inventoryMovementClass.getDeclaredField("batch");
        assertEquals(ProductBatch.class, batchField.getType());
        
        Field branchField = inventoryMovementClass.getDeclaredField("branch");
        assertEquals(Branch.class, branchField.getType());
        
        Field movementTypeField = inventoryMovementClass.getDeclaredField("movementType");
        assertEquals(MovementType.class, movementTypeField.getType());
        
        Field quantityField = inventoryMovementClass.getDeclaredField("quantity");
        assertEquals(Integer.class, quantityField.getType());
        
        Field movementDateField = inventoryMovementClass.getDeclaredField("movementDate");
        assertEquals(LocalDateTime.class, movementDateField.getType());
        
        Field reasonField = inventoryMovementClass.getDeclaredField("reason");
        assertEquals(String.class, reasonField.getType());
        
        Field userField = inventoryMovementClass.getDeclaredField("user");
        assertEquals(User.class, userField.getType());
    }
    
    @Test
    @DisplayName("Parameterized constructor must have all required parameters")
    void testParameterizedConstructorHasAllParameters() throws NoSuchMethodException {
        Class<?> inventoryMovementClass = InventoryMovement.class;
        
        Constructor<?> constructor = inventoryMovementClass.getDeclaredConstructor(
            Integer.class,      // movementId
            Product.class,      // product
            ProductBatch.class, // batch
            Branch.class,       // branch
            MovementType.class, // movementType
            Integer.class,      // quantity
            LocalDateTime.class, // movementDate
            String.class,       // reason
            User.class          // user
        );
        
        assertNotNull(constructor);
        assertEquals(9, constructor.getParameterCount());
    }

    @Test
    @DisplayName("InventoryMovement class must have all basic methods (getters, setters, equals, hashCode, toString)")
    void testAllBasicMethodsExist() throws NoSuchMethodException {
        Class<?> inventoryMovementClass = InventoryMovement.class;
        
        // Getters
        assertNotNull(inventoryMovementClass.getMethod("getMovementId"));
        assertNotNull(inventoryMovementClass.getMethod("getProduct"));
        assertNotNull(inventoryMovementClass.getMethod("getBatch"));
        assertNotNull(inventoryMovementClass.getMethod("getBranch"));
        assertNotNull(inventoryMovementClass.getMethod("getMovementType"));
        assertNotNull(inventoryMovementClass.getMethod("getQuantity"));
        assertNotNull(inventoryMovementClass.getMethod("getMovementDate"));
        assertNotNull(inventoryMovementClass.getMethod("getReason"));
        assertNotNull(inventoryMovementClass.getMethod("getUser"));
        
        // Setters
        assertNotNull(inventoryMovementClass.getMethod("setMovementId", Integer.class));
        assertNotNull(inventoryMovementClass.getMethod("setProduct", Product.class));
        assertNotNull(inventoryMovementClass.getMethod("setBatch", ProductBatch.class));
        assertNotNull(inventoryMovementClass.getMethod("setBranch", Branch.class));
        assertNotNull(inventoryMovementClass.getMethod("setMovementType", MovementType.class));
        assertNotNull(inventoryMovementClass.getMethod("setQuantity", Integer.class));
        assertNotNull(inventoryMovementClass.getMethod("setMovementDate", LocalDateTime.class));
        assertNotNull(inventoryMovementClass.getMethod("setReason", String.class));
        assertNotNull(inventoryMovementClass.getMethod("setUser", User.class));
        
        // equals, hashCode, toString
        assertNotNull(inventoryMovementClass.getMethod("equals", Object.class));
        assertNotNull(inventoryMovementClass.getMethod("hashCode"));
        assertNotNull(inventoryMovementClass.getMethod("toString"));
    }
    
    @Test
    @DisplayName("Builder pattern should work correctly")
    void testBuilderPattern() {
        Product product = Product.builder()
                .productId("TEST-TEST-TEST-001-TEST-001")
                .commercialName("Test Product")
                .manufacturer("Test Manufacturer")
                .build();
        
        ProductBatch batch = ProductBatch.builder()
                .batchNumber("BATCH001")
                .quantityReceived(100)
                .quantityAvailable(100)
                .build();
        
        Branch branch = Branch.builder()
                .branchId(1)
                .branchName("Main Branch")
                .build();
        
        User user = User.builder()
                .id("1234567890123")
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .email("john@example.com")
                .password("hashedpassword")
                .role(Role.STOREKEEPER)
                .build();
        
        InventoryMovement inventoryMovement = InventoryMovement.builder()
                .movementId(1)
                .product(product)
                .batch(batch)
                .branch(branch)
                .movementType(MovementType.IN)
                .quantity(50)
                .movementDate(LocalDateTime.of(2024, 3, 15, 10, 30))
                .reason("Initial stock receipt")
                .user(user)
                .build();
        
        assertNotNull(inventoryMovement);
        assertEquals(1, inventoryMovement.getMovementId());
        assertEquals(product, inventoryMovement.getProduct());
        assertEquals(batch, inventoryMovement.getBatch());
        assertEquals(branch, inventoryMovement.getBranch());
        assertEquals(MovementType.IN, inventoryMovement.getMovementType());
        assertEquals(50, inventoryMovement.getQuantity());
        assertEquals(LocalDateTime.of(2024, 3, 15, 10, 30), inventoryMovement.getMovementDate());
        assertEquals("Initial stock receipt", inventoryMovement.getReason());
        assertEquals(user, inventoryMovement.getUser());
    }
    
    @Test
    @DisplayName("Should handle different movement types correctly")
    void testDifferentMovementTypes() {
        Product product = Product.builder()
                .productId("TEST-TEST-TEST-002-TEST-001")
                .commercialName("Test Product 2")
                .manufacturer("Test Manufacturer")
                .build();
        
        // Test IN movement
        InventoryMovement inMovement = InventoryMovement.builder()
                .product(product)
                .movementType(MovementType.IN)
                .quantity(100)
                .reason("Stock receipt from supplier")
                .build();
        
        // Test OUT movement
        InventoryMovement outMovement = InventoryMovement.builder()
                .product(product)
                .movementType(MovementType.OUT)
                .quantity(25)
                .reason("Sale to customer")
                .build();
        
        // Test ADJUSTMENT movement
        InventoryMovement adjustmentMovement = InventoryMovement.builder()
                .product(product)
                .movementType(MovementType.ADJUSTMENT)
                .quantity(-5)
                .reason("Inventory count adjustment")
                .build();
        
        assertEquals(MovementType.IN, inMovement.getMovementType());
        assertEquals(100, inMovement.getQuantity());
        assertEquals("Stock receipt from supplier", inMovement.getReason());
        
        assertEquals(MovementType.OUT, outMovement.getMovementType());
        assertEquals(25, outMovement.getQuantity());
        assertEquals("Sale to customer", outMovement.getReason());
        
        assertEquals(MovementType.ADJUSTMENT, adjustmentMovement.getMovementType());
        assertEquals(-5, adjustmentMovement.getQuantity());
        assertEquals("Inventory count adjustment", adjustmentMovement.getReason());
    }
    
    @Test
    @DisplayName("Should handle optional fields correctly")
    void testOptionalFields() {
        Product product = Product.builder()
                .productId("TEST-TEST-TEST-003-TEST-001")
                .commercialName("Test Product 3")
                .manufacturer("Test Manufacturer")
                .build();
        
        // Movement with minimal required fields
        InventoryMovement minimalMovement = InventoryMovement.builder()
                .product(product)
                .movementType(MovementType.OUT)
                .quantity(10)
                .build();
        
        assertNotNull(minimalMovement);
        assertEquals(product, minimalMovement.getProduct());
        assertEquals(MovementType.OUT, minimalMovement.getMovementType());
        assertEquals(10, minimalMovement.getQuantity());
        
        // Optional fields should be null
        assertNull(minimalMovement.getBatch());
        assertNull(minimalMovement.getBranch());
        assertNull(minimalMovement.getReason());
        assertNull(minimalMovement.getUser());
        assertNull(minimalMovement.getMovementDate());
    }
    
    @Test
    @DisplayName("Should handle datetime fields correctly")
    void testDateTimeHandling() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime specificTime = LocalDateTime.of(2024, 6, 15, 14, 30, 45);
        
        Product product = Product.builder()
                .productId("TEST-TEST-TEST-004-TEST-001")
                .commercialName("Test Product 4")
                .manufacturer("Test Manufacturer")
                .build();
        
        InventoryMovement movement = InventoryMovement.builder()
                .product(product)
                .movementType(MovementType.ADJUSTMENT)
                .quantity(5)
                .movementDate(specificTime)
                .build();
        
        assertEquals(specificTime, movement.getMovementDate());
        
        // Test updating the date
        movement.setMovementDate(now);
        assertEquals(now, movement.getMovementDate());
    }
}