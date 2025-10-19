package com.mycompany.repository;

import com.mycompany.model.entity.*;
import com.mycompany.model.entity.enums.MovementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
 * Unit tests for InventoryMovementRepository
 * Tests database operations for InventoryMovement entity persistence
 * @author ramir
 */
public class InventoryMovementRepositoryTest {

    private InventoryMovementRepository repository;
    private EntityManager em;
    private TypedQuery<InventoryMovement> query;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        em = Mockito.mock(EntityManager.class);
        repository = new InventoryMovementRepository();
        query = Mockito.mock(TypedQuery.class);

        // Use reflection to inject the mocked EntityManager
        java.lang.reflect.Field field = repository.getClass().getSuperclass().getDeclaredField("em");
        field.setAccessible(true);
        field.set(repository, em);
    }

    @Test
    @DisplayName("Should save inventory movement to database")
    void testSave() {
        // Given
        InventoryMovement movement = createTestMovement();

        // When
        repository.save(movement);

        // Then
        Mockito.verify(em).persist(movement);
    }

    @Test
    @DisplayName("Should update existing inventory movement in database")
    void testUpdate() {
        // Given
        InventoryMovement movement = createTestMovement();

        // When
        repository.update(movement);

        // Then
        Mockito.verify(em).merge(movement);
    }

    @Test
    @DisplayName("Should find inventory movement by ID from database")
    void testFindById() {
        // Given
        Integer movementId = 1;
        InventoryMovement expectedMovement = createTestMovement();
        expectedMovement.setMovementId(movementId);

        Mockito.when(em.find(InventoryMovement.class, movementId)).thenReturn(expectedMovement);

        // When
        InventoryMovement result = repository.findById(movementId);

        // Then
        assertEquals(expectedMovement, result);
        assertEquals(movementId, result.getMovementId());
    }

    @Test
    @DisplayName("Should delete inventory movement from database")
    void testDelete() {
        // Given
        InventoryMovement movement = createTestMovement();

        Mockito.when(em.merge(movement)).thenReturn(movement);

        // When
        repository.delete(movement);

        // Then
        Mockito.verify(em).merge(movement);
        Mockito.verify(em).remove(movement);
    }

    @Test
    @DisplayName("Should find movements by product ordered by date descending")
    void testFindByProduct_ReturnsMovementsOrderedByDateDesc() {
        // Given
        Product product = createTestProduct();

        InventoryMovement movement1 = createTestMovement();
        movement1.setMovementId(1);
        movement1.setMovementDate(LocalDateTime.now().minusDays(2));

        InventoryMovement movement2 = createTestMovement();
        movement2.setMovementId(2);
        movement2.setMovementDate(LocalDateTime.now().minusDays(1));

        InventoryMovement movement3 = createTestMovement();
        movement3.setMovementId(3);
        movement3.setMovementDate(LocalDateTime.now());

        List<InventoryMovement> expectedMovements = Arrays.asList(movement3, movement2, movement1);

        Mockito.when(em.createQuery(anyString(), eq(InventoryMovement.class))).thenReturn(query);
        Mockito.when(query.setParameter("product", product)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(expectedMovements);

        // When
        List<InventoryMovement> result = repository.findByProduct(product);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        // Verify order: newest first
        assertEquals(3, result.get(0).getMovementId());
        assertEquals(2, result.get(1).getMovementId());
        assertEquals(1, result.get(2).getMovementId());

        // Verify query includes ORDER BY movementDate DESC
        Mockito.verify(em).createQuery(
            contains("ORDER BY im.movementDate DESC"),
            eq(InventoryMovement.class)
        );
    }

    @Test
    @DisplayName("Should return empty list when product has no movements")
    void testFindByProduct_WithNoMovements() {
        // Given
        Product product = createTestProduct();

        Mockito.when(em.createQuery(anyString(), eq(InventoryMovement.class))).thenReturn(query);
        Mockito.when(query.setParameter("product", product)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(Collections.emptyList());

        // When
        List<InventoryMovement> result = repository.findByProduct(product);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find movements by product ID")
    void testFindByProductId_ReturnsCorrectMovements() {
        // Given
        Long productId = 1L;

        InventoryMovement movement1 = createTestMovement();
        movement1.setMovementId(1);

        InventoryMovement movement2 = createTestMovement();
        movement2.setMovementId(2);

        List<InventoryMovement> expectedMovements = Arrays.asList(movement2, movement1);

        Mockito.when(em.createQuery(anyString(), eq(InventoryMovement.class))).thenReturn(query);
        Mockito.when(query.setParameter("productId", productId)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(expectedMovements);

        // When
        List<InventoryMovement> result = repository.findByProductId(productId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        Mockito.verify(query).setParameter("productId", productId);
    }

    @Test
    @DisplayName("Should use JOIN FETCH to eagerly load lazy relationships")
    void testFindByProduct_FetchesLazyRelations() {
        // Given
        Product product = createTestProduct();

        Mockito.when(em.createQuery(anyString(), eq(InventoryMovement.class))).thenReturn(query);
        Mockito.when(query.setParameter("product", product)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(Collections.emptyList());

        // When
        repository.findByProduct(product);

        // Then - Verify query uses JOIN FETCH for batch, branch, and user
        Mockito.verify(em).createQuery(
            contains("LEFT JOIN FETCH im.batch"),
            eq(InventoryMovement.class)
        );
        Mockito.verify(em).createQuery(
            contains("LEFT JOIN FETCH im.branch"),
            eq(InventoryMovement.class)
        );
        Mockito.verify(em).createQuery(
            contains("LEFT JOIN FETCH im.user"),
            eq(InventoryMovement.class)
        );
    }

    @Test
    @DisplayName("Should use JOIN FETCH in findByProductId query")
    void testFindByProductId_FetchesLazyRelations() {
        // Given
        Long productId = 1L;

        Mockito.when(em.createQuery(anyString(), eq(InventoryMovement.class))).thenReturn(query);
        Mockito.when(query.setParameter("productId", productId)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(Collections.emptyList());

        // When
        repository.findByProductId(productId);

        // Then - Verify query uses JOIN FETCH
        Mockito.verify(em).createQuery(
            contains("LEFT JOIN FETCH im.batch"),
            eq(InventoryMovement.class)
        );
        Mockito.verify(em).createQuery(
            contains("LEFT JOIN FETCH im.branch"),
            eq(InventoryMovement.class)
        );
        Mockito.verify(em).createQuery(
            contains("LEFT JOIN FETCH im.user"),
            eq(InventoryMovement.class)
        );
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
