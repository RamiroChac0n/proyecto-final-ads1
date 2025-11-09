package com.mycompany.service.impl;

import com.mycompany.model.entity.InventoryMovement;
import com.mycompany.model.entity.Product;
import com.mycompany.model.entity.enums.MovementType;
import com.mycompany.repository.InventoryMovementRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.*;

/**
 * Unit tests for InventoryMovementServiceImpl
 * Tests business logic for inventory movement operations
 * @author ramir
 */
public class InventoryMovementServiceImplTest {

    private InventoryMovementServiceImpl service;
    private InventoryMovementRepository repository;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        service = new InventoryMovementServiceImpl();
        repository = Mockito.mock(InventoryMovementRepository.class);

        // Use reflection to inject the mocked repository
        java.lang.reflect.Field field = service.getClass().getDeclaredField("inventoryMovementRepository");
        field.setAccessible(true);
        field.set(service, repository);
    }

    @Test
    @DisplayName("Should save inventory movement through repository")
    void testSave() {
        // Given
        InventoryMovement movement = createTestMovement();
        Mockito.when(repository.save(movement)).thenReturn(movement);

        // When
        InventoryMovement result = service.save(movement);

        // Then
        assertNotNull(result);
        assertEquals(movement, result);
        Mockito.verify(repository).save(movement);
    }

    @Test
    @DisplayName("Should edit inventory movement through repository")
    void testEdit() {
        // Given
        InventoryMovement movement = createTestMovement();
        movement.setMovementId(1);
        Mockito.when(repository.update(movement)).thenReturn(movement);

        // When
        InventoryMovement result = service.edit(movement);

        // Then
        assertNotNull(result);
        assertEquals(movement, result);
        Mockito.verify(repository).update(movement);
    }

    @Test
    @DisplayName("Should delete inventory movement through repository")
    void testDelete() {
        // Given
        InventoryMovement movement = createTestMovement();
        movement.setMovementId(1);

        // When
        service.delete(movement);

        // Then
        Mockito.verify(repository).delete(movement);
    }

    @Test
    @DisplayName("Should list all inventory movements")
    void testList() {
        // Given
        List<InventoryMovement> expectedMovements = Arrays.asList(
                createTestMovement(),
                createTestMovement()
        );
        Mockito.when(repository.findAll()).thenReturn(expectedMovements);

        // When
        List<InventoryMovement> result = service.list();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedMovements, result);
        Mockito.verify(repository).findAll();
    }

    @Test
    @DisplayName("Should find inventory movement by ID")
    void testFindById() {
        // Given
        Integer movementId = 1;
        InventoryMovement expectedMovement = createTestMovement();
        expectedMovement.setMovementId(movementId);
        Mockito.when(repository.findById(movementId)).thenReturn(expectedMovement);

        // When
        InventoryMovement result = service.findById(movementId);

        // Then
        assertNotNull(result);
        assertEquals(movementId, result.getMovementId());
        assertEquals(expectedMovement, result);
        Mockito.verify(repository).findById(movementId);
    }

    @Test
    @DisplayName("Should return null when movement not found by ID")
    void testFindById_NotFound() {
        // Given
        Integer movementId = 999;
        Mockito.when(repository.findById(movementId)).thenReturn(null);

        // When
        InventoryMovement result = service.findById(movementId);

        // Then
        assertNull(result);
        Mockito.verify(repository).findById(movementId);
    }

    @Test
    @DisplayName("Should find movements by product")
    void testFindByProduct() {
        // Given
        Product product = createTestProduct();
        List<InventoryMovement> expectedMovements = Arrays.asList(
                createTestMovement(),
                createTestMovement(),
                createTestMovement()
        );
        Mockito.when(repository.findByProduct(product)).thenReturn(expectedMovements);

        // When
        List<InventoryMovement> result = service.findByProduct(product);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedMovements, result);
        Mockito.verify(repository).findByProduct(product);
    }

    @Test
    @DisplayName("Should return empty list when product has no movements")
    void testFindByProduct_NoMovements() {
        // Given
        Product product = createTestProduct();
        Mockito.when(repository.findByProduct(product)).thenReturn(Collections.emptyList());

        // When
        List<InventoryMovement> result = service.findByProduct(product);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        Mockito.verify(repository).findByProduct(product);
    }

    @Test
    @DisplayName("Should find movements by product ID")
    void testFindByProductId() {
        // Given
        Long productId = 1L;
        List<InventoryMovement> expectedMovements = Arrays.asList(
                createTestMovement(),
                createTestMovement()
        );
        Mockito.when(repository.findByProductId(productId)).thenReturn(expectedMovements);

        // When
        List<InventoryMovement> result = service.findByProductId(productId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedMovements, result);
        Mockito.verify(repository).findByProductId(productId);
    }

    @Test
    @DisplayName("Should return empty list when product ID has no movements")
    void testFindByProductId_NoMovements() {
        // Given
        Long productId = 999L;
        Mockito.when(repository.findByProductId(productId)).thenReturn(Collections.emptyList());

        // When
        List<InventoryMovement> result = service.findByProductId(productId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        Mockito.verify(repository).findByProductId(productId);
    }

    // Helper methods
    private InventoryMovement createTestMovement() {
        return InventoryMovement.builder()
                .product(createTestProduct())
                .movementType(MovementType.IN)
                .quantity(10)
                .movementDate(LocalDateTime.now())
                .reason("Test movement")
                .build();
    }

    private Product createTestProduct() {
        return Product.builder()
                .productId(1L)
                .commercialName("Test Product")
                .manufacturer("Test Manufacturer")
                .build();
    }
}
