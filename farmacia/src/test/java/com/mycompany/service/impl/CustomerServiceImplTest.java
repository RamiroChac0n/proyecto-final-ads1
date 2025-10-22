package com.mycompany.service.impl;

import com.mycompany.model.entity.Customer;
import com.mycompany.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerServiceImpl
 * Tests all CRUD operations, searches, and business rules
 *
 * @author ramir
 */
@DisplayName("CustomerService Tests")
class CustomerServiceImplTest {

    private CustomerServiceImpl customerService;
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        customerService = new CustomerServiceImpl();
        customerRepository = Mockito.mock(CustomerRepository.class);

        // Use reflection to inject the mock repository
        java.lang.reflect.Field field = customerService.getClass().getDeclaredField("customerRepository");
        field.setAccessible(true);
        field.set(customerService, customerRepository);
    }

    // ==================== SAVE (CREATE) TESTS ====================

    @Test
    @DisplayName("save() - Valid customer saves successfully")
    void save_ValidCustomer_SavesSuccessfully() {
        // Given
        Customer customer = Customer.builder()
            .taxId("12345678")
            .customerName("Juan Pérez")
            .address("Ciudad Guatemala")
            .phone("12345678")
            .build();

        when(customerRepository.taxIdExists(anyString(), isNull())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // When
        Customer result = customerService.save(customer);

        // Then
        assertNotNull(result);
        assertEquals("12345678", result.getTaxId());
        assertEquals("Juan Pérez", result.getCustomerName());
        assertTrue(result.getIsActive());
        verify(customerRepository).save(customer);
        verify(customerRepository).taxIdExists("12345678", null);
    }

    @Test
    @DisplayName("save() - Duplicate tax ID throws exception")
    void save_DuplicateTaxId_ThrowsException() {
        // Given
        Customer customer = Customer.builder()
            .taxId("12345678")
            .customerName("Juan Pérez")
            .build();

        when(customerRepository.taxIdExists("12345678", null)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> customerService.save(customer)
        );

        assertTrue(exception.getMessage().contains("Ya existe un cliente con el NIT"));
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("save() - Sets isActive to true when null")
    void save_NullIsActive_SetsToTrue() {
        // Given
        Customer customer = Customer.builder()
            .taxId("12345678")
            .customerName("Juan Pérez")
            .isActive(null)
            .build();

        when(customerRepository.taxIdExists(anyString(), isNull())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            assertNotNull(c.getIsActive());
            assertTrue(c.getIsActive());
            return c;
        });

        // When
        customerService.save(customer);

        // Then
        verify(customerRepository).save(argThat(c -> c.getIsActive() != null && c.getIsActive()));
    }

    // ==================== EDIT (UPDATE) TESTS ====================

    @Test
    @DisplayName("edit() - Valid customer updates successfully")
    void edit_ValidCustomer_UpdatesSuccessfully() {
        // Given
        Customer customer = Customer.builder()
            .customerId(1)
            .taxId("12345678")
            .customerName("Juan Pérez Updated")
            .build();

        when(customerRepository.taxIdExists("12345678", 1)).thenReturn(false);
        when(customerRepository.update(any(Customer.class))).thenReturn(customer);

        // When
        Customer result = customerService.edit(customer);

        // Then
        assertNotNull(result);
        assertEquals("Juan Pérez Updated", result.getCustomerName());
        verify(customerRepository).update(customer);
        verify(customerRepository).taxIdExists("12345678", 1);
    }

    @Test
    @DisplayName("edit() - Changing tax ID to duplicate throws exception")
    void edit_ChangeTaxIdToDuplicate_ThrowsException() {
        // Given
        Customer customer = Customer.builder()
            .customerId(1)
            .taxId("87654321")
            .customerName("Juan Pérez")
            .build();

        when(customerRepository.taxIdExists("87654321", 1)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> customerService.edit(customer)
        );

        assertTrue(exception.getMessage().contains("Ya existe otro cliente con el NIT"));
        verify(customerRepository, never()).update(any());
    }

    @Test
    @DisplayName("edit() - Updating with same tax ID succeeds")
    void edit_SameTaxId_UpdatesSuccessfully() {
        // Given
        Customer customer = Customer.builder()
            .customerId(1)
            .taxId("12345678")
            .customerName("Juan Pérez")
            .build();

        when(customerRepository.taxIdExists("12345678", 1)).thenReturn(false);
        when(customerRepository.update(any(Customer.class))).thenReturn(customer);

        // When
        Customer result = customerService.edit(customer);

        // Then
        assertNotNull(result);
        verify(customerRepository).update(customer);
    }

    // ==================== DELETE (SOFT DELETE) TESTS ====================

    @Test
    @DisplayName("delete() - Sets isActive to false (soft delete)")
    void delete_ValidCustomer_SetsIsActiveFalse() {
        // Given
        Customer customer = Customer.builder()
            .customerId(1)
            .taxId("12345678")
            .customerName("Juan Pérez")
            .isActive(true)
            .build();

        when(customerRepository.update(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            assertFalse(c.getIsActive());
            return c;
        });

        // When
        customerService.delete(customer);

        // Then
        assertFalse(customer.getIsActive());
        verify(customerRepository).update(argThat(c -> !c.getIsActive()));
    }

    @Test
    @DisplayName("delete() - Does not physically delete from database")
    void delete_Customer_CallsUpdateNotDelete() {
        // Given
        Customer customer = Customer.builder()
            .customerId(1)
            .taxId("12345678")
            .customerName("Juan Pérez")
            .isActive(true)
            .build();

        // When
        customerService.delete(customer);

        // Then
        verify(customerRepository).update(any());
        verify(customerRepository, never()).delete(any());
    }

    // ==================== SEARCH TESTS ====================

    @Test
    @DisplayName("findByTaxId() - Existing tax ID returns customer")
    void findByTaxId_ExistingTaxId_ReturnsCustomer() {
        // Given
        Customer expected = Customer.builder()
            .customerId(1)
            .taxId("12345678")
            .customerName("Juan Pérez")
            .build();

        when(customerRepository.findByTaxId("12345678")).thenReturn(expected);

        // When
        Customer result = customerService.findByTaxId("12345678");

        // Then
        assertNotNull(result);
        assertEquals("12345678", result.getTaxId());
        assertEquals("Juan Pérez", result.getCustomerName());
        verify(customerRepository).findByTaxId("12345678");
    }

    @Test
    @DisplayName("findByTaxId() - Non-existing tax ID returns null")
    void findByTaxId_NonExistingTaxId_ReturnsNull() {
        // Given
        when(customerRepository.findByTaxId("99999999")).thenReturn(null);

        // When
        Customer result = customerService.findByTaxId("99999999");

        // Then
        assertNull(result);
        verify(customerRepository).findByTaxId("99999999");
    }

    @Test
    @DisplayName("findByTaxId() - Null tax ID returns null")
    void findByTaxId_NullTaxId_ReturnsNull() {
        // When
        Customer result = customerService.findByTaxId(null);

        // Then
        assertNull(result);
        verify(customerRepository, never()).findByTaxId(anyString());
    }

    @Test
    @DisplayName("findByTaxId() - Empty tax ID returns null")
    void findByTaxId_EmptyTaxId_ReturnsNull() {
        // When
        Customer result = customerService.findByTaxId("   ");

        // Then
        assertNull(result);
        verify(customerRepository, never()).findByTaxId(anyString());
    }

    @Test
    @DisplayName("findByTaxId() - Trims whitespace before search")
    void findByTaxId_WithWhitespace_TrimsBeforeSearch() {
        // Given
        Customer expected = Customer.builder()
            .taxId("12345678")
            .customerName("Juan Pérez")
            .build();

        when(customerRepository.findByTaxId("12345678")).thenReturn(expected);

        // When
        Customer result = customerService.findByTaxId("  12345678  ");

        // Then
        assertNotNull(result);
        verify(customerRepository).findByTaxId("12345678");
    }

    @Test
    @DisplayName("findByPhone() - Existing phone returns customer")
    void findByPhone_ExistingPhone_ReturnsCustomer() {
        // Given
        Customer expected = Customer.builder()
            .customerId(1)
            .phone("12345678")
            .customerName("Juan Pérez")
            .build();

        when(customerRepository.findByPhone("12345678")).thenReturn(expected);

        // When
        Customer result = customerService.findByPhone("12345678");

        // Then
        assertNotNull(result);
        assertEquals("12345678", result.getPhone());
        verify(customerRepository).findByPhone("12345678");
    }

    @Test
    @DisplayName("findByPhone() - Non-existing phone returns null")
    void findByPhone_NonExistingPhone_ReturnsNull() {
        // Given
        when(customerRepository.findByPhone("99999999")).thenReturn(null);

        // When
        Customer result = customerService.findByPhone("99999999");

        // Then
        assertNull(result);
        verify(customerRepository).findByPhone("99999999");
    }

    @Test
    @DisplayName("findByPhone() - Null phone returns null")
    void findByPhone_NullPhone_ReturnsNull() {
        // When
        Customer result = customerService.findByPhone(null);

        // Then
        assertNull(result);
        verify(customerRepository, never()).findByPhone(anyString());
    }

    @Test
    @DisplayName("findByPhone() - Empty phone returns null")
    void findByPhone_EmptyPhone_ReturnsNull() {
        // When
        Customer result = customerService.findByPhone("   ");

        // Then
        assertNull(result);
        verify(customerRepository, never()).findByPhone(anyString());
    }

    // ==================== LIST TESTS ====================

    @Test
    @DisplayName("list() - Returns only active customers")
    void list_MultipleCustomers_ReturnsOnlyActive() {
        // Given
        Customer activeCustomer1 = Customer.builder()
            .customerId(1)
            .taxId("11111111")
            .customerName("Active Customer 1")
            .isActive(true)
            .build();

        Customer activeCustomer2 = Customer.builder()
            .customerId(2)
            .taxId("22222222")
            .customerName("Active Customer 2")
            .isActive(true)
            .build();

        Customer inactiveCustomer = Customer.builder()
            .customerId(3)
            .taxId("33333333")
            .customerName("Inactive Customer")
            .isActive(false)
            .build();

        when(customerRepository.findAll()).thenReturn(
            Arrays.asList(activeCustomer1, activeCustomer2, inactiveCustomer)
        );

        // When
        List<Customer> result = customerService.list();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Customer::getIsActive));
        assertTrue(result.stream().anyMatch(c -> c.getCustomerId().equals(1)));
        assertTrue(result.stream().anyMatch(c -> c.getCustomerId().equals(2)));
        assertFalse(result.stream().anyMatch(c -> c.getCustomerId().equals(3)));
    }

    @Test
    @DisplayName("list() - Empty repository returns empty list")
    void list_NoCustomers_ReturnsEmptyList() {
        // Given
        when(customerRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Customer> result = customerService.list();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("list() - All inactive customers returns empty list")
    void list_AllInactive_ReturnsEmptyList() {
        // Given
        Customer inactiveCustomer1 = Customer.builder()
            .customerId(1)
            .isActive(false)
            .build();

        Customer inactiveCustomer2 = Customer.builder()
            .customerId(2)
            .isActive(false)
            .build();

        when(customerRepository.findAll()).thenReturn(
            Arrays.asList(inactiveCustomer1, inactiveCustomer2)
        );

        // When
        List<Customer> result = customerService.list();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== FINDBYID TEST ====================

    @Test
    @DisplayName("findById() - Existing ID returns customer")
    void findById_ExistingId_ReturnsCustomer() {
        // Given
        Customer expected = Customer.builder()
            .customerId(1)
            .taxId("12345678")
            .customerName("Juan Pérez")
            .build();

        when(customerRepository.findById(1)).thenReturn(expected);

        // When
        Customer result = customerService.findById(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCustomerId());
        verify(customerRepository).findById(1);
    }

    @Test
    @DisplayName("findById() - Non-existing ID returns null")
    void findById_NonExistingId_ReturnsNull() {
        // Given
        when(customerRepository.findById(999)).thenReturn(null);

        // When
        Customer result = customerService.findById(999);

        // Then
        assertNull(result);
        verify(customerRepository).findById(999);
    }
}
