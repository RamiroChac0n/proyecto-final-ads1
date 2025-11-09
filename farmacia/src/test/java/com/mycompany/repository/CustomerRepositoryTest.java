package com.mycompany.repository;

import com.mycompany.model.entity.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerRepository
 * Tests JPQL queries and database interactions
 *
 * @author ramir
 */
@DisplayName("CustomerRepository Tests")
class CustomerRepositoryTest {

    private CustomerRepository customerRepository;
    private EntityManager entityManager;
    private TypedQuery<Customer> typedQuery;
    private TypedQuery<Long> countQuery;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        customerRepository = new CustomerRepository();
        entityManager = Mockito.mock(EntityManager.class);
        typedQuery = Mockito.mock(TypedQuery.class);
        countQuery = Mockito.mock(TypedQuery.class);

        // Inject mock EntityManager via reflection
        java.lang.reflect.Field field = customerRepository.getClass().getDeclaredField("em");
        field.setAccessible(true);
        field.set(customerRepository, entityManager);
    }

    // ==================== FIND BY TAX ID TESTS ====================

    @Test
    @DisplayName("findByTaxId() - Existing tax ID returns customer")
    void findByTaxId_ExistingTaxId_ReturnsCustomer() {
        // Given
        Customer expected = Customer.builder()
            .customerId(1)
            .taxId("12345678")
            .customerName("Juan Pérez")
            .isActive(true)
            .build();

        when(entityManager.createQuery(anyString(), eq(Customer.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("taxId"), eq("12345678"))).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(expected);

        // When
        Customer result = customerRepository.findByTaxId("12345678");

        // Then
        assertNotNull(result);
        assertEquals("12345678", result.getTaxId());
        assertEquals("Juan Pérez", result.getCustomerName());
        assertTrue(result.getIsActive());

        verify(entityManager).createQuery(
            "SELECT c FROM Customer c WHERE c.taxId = :taxId AND c.isActive = true",
            Customer.class
        );
        verify(typedQuery).setParameter("taxId", "12345678");
        verify(typedQuery).getSingleResult();
    }

    @Test
    @DisplayName("findByTaxId() - Non-existing tax ID returns null")
    void findByTaxId_NonExistingTaxId_ReturnsNull() {
        // Given
        when(entityManager.createQuery(anyString(), eq(Customer.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), anyString())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());

        // When
        Customer result = customerRepository.findByTaxId("99999999");

        // Then
        assertNull(result);
        verify(typedQuery).getSingleResult();
    }

    @Test
    @DisplayName("findByTaxId() - Only returns active customers")
    void findByTaxId_VerifiesActiveFlag() {
        // Given
        when(entityManager.createQuery(anyString(), eq(Customer.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), anyString())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());

        // When
        customerRepository.findByTaxId("12345678");

        // Then
        verify(entityManager).createQuery(
            argThat(query -> query.contains("c.isActive = true")),
            eq(Customer.class)
        );
    }

    // ==================== FIND BY PHONE TESTS ====================

    @Test
    @DisplayName("findByPhone() - Existing phone returns customer")
    void findByPhone_ExistingPhone_ReturnsCustomer() {
        // Given
        Customer expected = Customer.builder()
            .customerId(1)
            .phone("12345678")
            .customerName("Juan Pérez")
            .isActive(true)
            .build();

        when(entityManager.createQuery(anyString(), eq(Customer.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("phone"), eq("12345678"))).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(expected);

        // When
        Customer result = customerRepository.findByPhone("12345678");

        // Then
        assertNotNull(result);
        assertEquals("12345678", result.getPhone());
        assertTrue(result.getIsActive());

        verify(entityManager).createQuery(
            "SELECT c FROM Customer c WHERE c.phone = :phone AND c.isActive = true",
            Customer.class
        );
        verify(typedQuery).setParameter("phone", "12345678");
    }

    @Test
    @DisplayName("findByPhone() - Non-existing phone returns null")
    void findByPhone_NonExistingPhone_ReturnsNull() {
        // Given
        when(entityManager.createQuery(anyString(), eq(Customer.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), anyString())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());

        // When
        Customer result = customerRepository.findByPhone("99999999");

        // Then
        assertNull(result);
        verify(typedQuery).getSingleResult();
    }

    // ==================== TAX ID EXISTS TESTS ====================

    @Test
    @DisplayName("taxIdExists() - Existing tax ID returns true")
    void taxIdExists_ExistingTaxId_ReturnsTrue() {
        // Given
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("taxId"), eq("12345678"))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        // When
        boolean result = customerRepository.taxIdExists("12345678", null);

        // Then
        assertTrue(result);
        verify(entityManager).createQuery(
            "SELECT COUNT(c) FROM Customer c WHERE c.taxId = :taxId",
            Long.class
        );
    }

    @Test
    @DisplayName("taxIdExists() - Non-existing tax ID returns false")
    void taxIdExists_NonExistingTaxId_ReturnsFalse() {
        // Given
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("taxId"), anyString())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);

        // When
        boolean result = customerRepository.taxIdExists("99999999", null);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("taxIdExists() - With exclusion excludes current customer")
    void taxIdExists_WithExclusion_ExcludesCurrentCustomer() {
        // Given
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("taxId"), eq("12345678"))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("customerId"), eq(1))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);

        // When
        boolean result = customerRepository.taxIdExists("12345678", 1);

        // Then
        assertFalse(result);
        verify(entityManager).createQuery(
            argThat(query -> query.contains("AND c.customerId != :customerId")),
            eq(Long.class)
        );
        verify(countQuery).setParameter("customerId", 1);
    }

    @Test
    @DisplayName("taxIdExists() - With null exclusion does not add exclusion clause")
    void taxIdExists_NullExclusion_NoExclusionClause() {
        // Given
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("taxId"), anyString())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);

        // When
        customerRepository.taxIdExists("12345678", null);

        // Then
        verify(entityManager).createQuery(
            argThat(query -> !query.contains("customerId")),
            eq(Long.class)
        );
        verify(countQuery, never()).setParameter(eq("customerId"), any());
    }

    @Test
    @DisplayName("taxIdExists() - Exception returns false")
    void taxIdExists_Exception_ReturnsFalse() {
        // Given
        when(entityManager.createQuery(anyString(), eq(Long.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When
        boolean result = customerRepository.taxIdExists("12345678", null);

        // Then
        assertFalse(result);
    }

    // ==================== INHERITED METHODS TESTS ====================

    @Test
    @DisplayName("save() - Persists customer successfully")
    void save_ValidCustomer_PersistsSuccessfully() {
        // Given
        Customer customer = Customer.builder()
            .taxId("12345678")
            .customerName("Juan Pérez")
            .build();

        // When
        Customer result = customerRepository.save(customer);

        // Then
        assertNotNull(result);
        verify(entityManager).persist(customer);
    }

    @Test
    @DisplayName("update() - Merges customer successfully")
    void update_ValidCustomer_MergesSuccessfully() {
        // Given
        Customer customer = Customer.builder()
            .customerId(1)
            .taxId("12345678")
            .customerName("Juan Pérez Updated")
            .build();

        when(entityManager.merge(any(Customer.class))).thenReturn(customer);

        // When
        Customer result = customerRepository.update(customer);

        // Then
        assertNotNull(result);
        assertEquals("Juan Pérez Updated", result.getCustomerName());
        verify(entityManager).merge(customer);
    }

    @Test
    @DisplayName("findById() - Returns customer when found")
    void findById_ExistingId_ReturnsCustomer() {
        // Given
        Customer expected = Customer.builder()
            .customerId(1)
            .taxId("12345678")
            .customerName("Juan Pérez")
            .build();

        when(entityManager.find(Customer.class, 1)).thenReturn(expected);

        // When
        Customer result = customerRepository.findById(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCustomerId());
        verify(entityManager).find(Customer.class, 1);
    }

    @Test
    @DisplayName("findById() - Returns null when not found")
    void findById_NonExistingId_ReturnsNull() {
        // Given
        when(entityManager.find(Customer.class, 999)).thenReturn(null);

        // When
        Customer result = customerRepository.findById(999);

        // Then
        assertNull(result);
        verify(entityManager).find(Customer.class, 999);
    }
}
