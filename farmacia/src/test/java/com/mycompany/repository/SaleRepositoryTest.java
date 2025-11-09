package com.mycompany.repository;

import com.mycompany.model.entity.*;
import com.mycompany.model.entity.enums.SaleStatus;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SaleRepository
 * Focus on new methods with JOIN FETCH to prevent LazyInitializationException
 * @author ramir
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SaleRepository Tests - Preventing LazyInitializationException")
class SaleRepositoryTest {

    private SaleRepository repository;
    private EntityManager em;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        em = mock(EntityManager.class);
        repository = new SaleRepository();
        injectEntityManager(repository, em);
    }

    /**
     * Helper method to inject EntityManager mock using reflection
     * Necessary because SaleRepository directly accesses the protected 'em' field
     */
    private void injectEntityManager(SaleRepository repository, EntityManager em)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = PharmacyRepository.class.getDeclaredField("em");
        field.setAccessible(true);
        field.set(repository, em);
    }

    /**
     * Test 1: findAllWithUser should use JOIN FETCH query
     * This prevents LazyInitializationException when accessing sale.user in view
     */
    @Test
    @DisplayName("Test 1: findAllWithUser should execute JOIN FETCH query correctly")
    void testFindAllWithUser_UsesJoinFetch() {
        // Given
        TypedQuery<Sale> mockQuery = mock(TypedQuery.class);
        List<Sale> expectedSales = createTestSales(3);

        when(em.createQuery(
            eq("SELECT s FROM Sale s LEFT JOIN FETCH s.user ORDER BY s.saleDate DESC"),
            eq(Sale.class)
        )).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedSales);

        // When
        List<Sale> result = repository.findAllWithUser();

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.size(), "Should return 3 sales");
        verify(em).createQuery(
            eq("SELECT s FROM Sale s LEFT JOIN FETCH s.user ORDER BY s.saleDate DESC"),
            eq(Sale.class)
        );
        verify(mockQuery).getResultList();
    }

    /**
     * Test 2: findAllWithUser should return empty list on exception
     */
    @Test
    @DisplayName("Test 2: findAllWithUser should return empty list on exception")
    void testFindAllWithUser_OnException_ReturnsEmptyList() {
        // Given
        when(em.createQuery(anyString(), eq(Sale.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When
        List<Sale> result = repository.findAllWithUser();

        // Then
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Should return empty list on exception");
    }

    /**
     * Test 3: findByIdWithDetails should use multiple JOIN FETCH
     * This loads User, SaleDetails, and Products in one query
     */
    @Test
    @DisplayName("Test 3: findByIdWithDetails should load all relationships with JOIN FETCH")
    void testFindByIdWithDetails_LoadsAllRelationships() {
        // Given
        Integer saleId = 1;
        TypedQuery<Sale> mockQuery = mock(TypedQuery.class);
        Sale expectedSale = createTestSaleWithDetails();

        String expectedJPQL = "SELECT DISTINCT s FROM Sale s " +
                            "LEFT JOIN FETCH s.user " +
                            "LEFT JOIN FETCH s.saleDetails sd " +
                            "LEFT JOIN FETCH sd.product " +
                            "WHERE s.saleId = :saleId";

        when(em.createQuery(eq(expectedJPQL), eq(Sale.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("saleId"), eq(saleId))).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(expectedSale);

        // When
        Sale result = repository.findByIdWithDetails(saleId);

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(expectedSale.getSaleId(), result.getSaleId());
        assertNotNull(result.getUser(), "User should be loaded");
        assertNotNull(result.getSaleDetails(), "SaleDetails should be loaded");

        verify(em).createQuery(eq(expectedJPQL), eq(Sale.class));
        verify(mockQuery).setParameter("saleId", saleId);
        verify(mockQuery).getSingleResult();
    }

    /**
     * Test 4: findByIdWithDetails should return null when sale not found
     */
    @Test
    @DisplayName("Test 4: findByIdWithDetails should return null when sale not found")
    void testFindByIdWithDetails_NotFound_ReturnsNull() {
        // Given
        Integer saleId = 999;
        TypedQuery<Sale> mockQuery = mock(TypedQuery.class);

        when(em.createQuery(anyString(), eq(Sale.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenThrow(new NoResultException());

        // When
        Sale result = repository.findByIdWithDetails(saleId);

        // Then
        assertNull(result, "Should return null when sale not found");
    }

    /**
     * Test 5: findBySaleNumber should execute correct query
     */
    @Test
    @DisplayName("Test 5: findBySaleNumber should find sale by sale number")
    void testFindBySaleNumber_Found() {
        // Given
        String saleNumber = "VEN-20250118120000";
        TypedQuery<Sale> mockQuery = mock(TypedQuery.class);
        Sale expectedSale = createTestSale(1, saleNumber);

        when(em.createQuery(
            eq("SELECT s FROM Sale s WHERE s.saleNumber = :saleNumber"),
            eq(Sale.class)
        )).thenReturn(mockQuery);
        when(mockQuery.setParameter("saleNumber", saleNumber)).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(expectedSale);

        // When
        Sale result = repository.findBySaleNumber(saleNumber);

        // Then
        assertNotNull(result);
        assertEquals(saleNumber, result.getSaleNumber());
        verify(mockQuery).setParameter("saleNumber", saleNumber);
    }

    /**
     * Test 6: findBySaleNumber should return null when not found
     */
    @Test
    @DisplayName("Test 6: findBySaleNumber should return null when not found")
    void testFindBySaleNumber_NotFound_ReturnsNull() {
        // Given
        String saleNumber = "INVALID-NUMBER";
        TypedQuery<Sale> mockQuery = mock(TypedQuery.class);

        when(em.createQuery(anyString(), eq(Sale.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenThrow(new NoResultException());

        // When
        Sale result = repository.findBySaleNumber(saleNumber);

        // Then
        assertNull(result);
    }

    /**
     * Test 7: findByDateRange should filter sales correctly
     */
    @Test
    @DisplayName("Test 7: findByDateRange should filter sales by date range")
    void testFindByDateRange() {
        // Given
        Date fromDate = new Date(2025, 0, 1);
        Date toDate = new Date(2025, 0, 31);
        TypedQuery<Sale> mockQuery = mock(TypedQuery.class);
        List<Sale> expectedSales = createTestSales(2);

        when(em.createQuery(
            eq("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :fromDate AND :toDate ORDER BY s.saleDate DESC"),
            eq(Sale.class)
        )).thenReturn(mockQuery);
        when(mockQuery.setParameter("fromDate", fromDate)).thenReturn(mockQuery);
        when(mockQuery.setParameter("toDate", toDate)).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedSales);

        // When
        List<Sale> result = repository.findByDateRange(fromDate, toDate);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(mockQuery).setParameter("fromDate", fromDate);
        verify(mockQuery).setParameter("toDate", toDate);
    }

    /**
     * Test 8: findByStatus should filter sales by status
     */
    @Test
    @DisplayName("Test 8: findByStatus should filter sales by status")
    void testFindByStatus() {
        // Given
        SaleStatus status = SaleStatus.COMPLETED;
        TypedQuery<Sale> mockQuery = mock(TypedQuery.class);
        List<Sale> expectedSales = createTestSales(1);

        when(em.createQuery(
            eq("SELECT s FROM Sale s WHERE s.saleStatus = :status ORDER BY s.saleDate DESC"),
            eq(Sale.class)
        )).thenReturn(mockQuery);
        when(mockQuery.setParameter("status", status)).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedSales);

        // When
        List<Sale> result = repository.findByStatus(status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mockQuery).setParameter("status", status);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Create a list of test sales
     */
    private List<Sale> createTestSales(int count) {
        List<Sale> sales = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            sales.add(createTestSale(i, "VEN-" + i));
        }
        return sales;
    }

    /**
     * Create a single test sale
     */
    private Sale createTestSale(Integer id, String saleNumber) {
        User user = User.builder()
            .id("USR123")
            .firstName("John")
            .lastName("Doe")
            .build();

        return Sale.builder()
            .saleId(id)
            .saleNumber(saleNumber)
            .saleDate(new Date())
            .user(user)
            .subtotal(new BigDecimal("100.00"))
            .totalAmount(new BigDecimal("100.00"))
            .cashReceived(new BigDecimal("100.00"))
            .changeGiven(BigDecimal.ZERO)
            .saleStatus(SaleStatus.COMPLETED)
            .build();
    }

    /**
     * Create a test sale with details
     */
    private Sale createTestSaleWithDetails() {
        User user = User.builder()
            .id("USR123")
            .firstName("John")
            .lastName("Doe")
            .build();

        Product product1 = Product.builder()
            .productId(1L)
            .commercialName("Product 1")
            .build();

        Product product2 = Product.builder()
            .productId(2L)
            .commercialName("Product 2")
            .build();

        SaleDetail detail1 = SaleDetail.builder()
            .detailId(1)
            .product(product1)
            .quantity(5)
            .unitPrice(new BigDecimal("10.00"))
            .lineTotal(new BigDecimal("50.00"))
            .build();

        SaleDetail detail2 = SaleDetail.builder()
            .detailId(2)
            .product(product2)
            .quantity(3)
            .unitPrice(new BigDecimal("20.00"))
            .lineTotal(new BigDecimal("60.00"))
            .build();

        Sale sale = Sale.builder()
            .saleId(1)
            .saleNumber("VEN-001")
            .saleDate(new Date())
            .user(user)
            .subtotal(new BigDecimal("110.00"))
            .totalAmount(new BigDecimal("110.00"))
            .cashReceived(new BigDecimal("120.00"))
            .changeGiven(new BigDecimal("10.00"))
            .saleStatus(SaleStatus.COMPLETED)
            .saleDetails(Arrays.asList(detail1, detail2))
            .build();

        // Set bidirectional relationships
        detail1.setSale(sale);
        detail2.setSale(sale);

        return sale;
    }
}
