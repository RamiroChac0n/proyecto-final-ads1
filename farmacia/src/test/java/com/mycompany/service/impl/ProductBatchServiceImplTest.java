package com.mycompany.service.impl;

import com.mycompany.model.entity.*;
import com.mycompany.model.entity.enums.Role;
import com.mycompany.repository.ProductBatchRepository;
import com.mycompany.service.IInventoryMovementService;
import com.mycompany.service.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductBatchServiceImpl
 * Tests business logic for adding batches to existing products
 * @author ramir
 */
@ExtendWith(MockitoExtension.class)
public class ProductBatchServiceImplTest {

    private ProductBatchServiceImpl service;

    @Mock
    private ProductBatchRepository productBatchRepository;

    @Mock
    private IProductService productService;

    @Mock
    private IInventoryMovementService inventoryMovementService;

    private Product testProduct;
    private ProductBatch testBatch;
    private User testUser;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        service = new ProductBatchServiceImpl();

        // Use reflection to inject the mock dependencies
        injectMock(service, "productBatchRepository", productBatchRepository);
        injectMock(service, "productService", productService);
        injectMock(service, "inventoryMovementService", inventoryMovementService);

        testProduct = createTestProduct();
        testBatch = createTestProductBatch();
        testUser = createTestUser();
    }

    @Test
    @DisplayName("Should add batch to existing product successfully")
    void testAddBatchToExistingProduct_Success() {
        // Given
        Long productId = 1L;
        ProductBatch batchToAdd = createNewProductBatch();
        ProductBatch savedBatch = createNewProductBatch();
        savedBatch.setBatchId(1);

        when(productService.findById(productId)).thenReturn(testProduct);
        when(productBatchRepository.existsByProductAndBatchNumber(testProduct, "BATCH002")).thenReturn(false);
        when(productBatchRepository.save(any(ProductBatch.class))).thenReturn(savedBatch);
        when(inventoryMovementService.save(any(InventoryMovement.class))).thenReturn(null);

        // When
        ProductBatch result = service.addBatchToExistingProduct(productId, batchToAdd, testUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getBatchId());
        assertEquals("BATCH002", result.getBatchNumber());

        // Verify that the product was set on the batch
        ArgumentCaptor<ProductBatch> batchCaptor = ArgumentCaptor.forClass(ProductBatch.class);
        verify(productBatchRepository).save(batchCaptor.capture());
        ProductBatch capturedBatch = batchCaptor.getValue();
        assertEquals(testProduct, capturedBatch.getProduct());

        verify(productService).findById(productId);
        verify(productBatchRepository).existsByProductAndBatchNumber(testProduct, "BATCH002");
    }

    @Test
    @DisplayName("Should throw exception when product does not exist")
    void testAddBatchToExistingProduct_ProductNotFound() {
        // Given
        Long productId = 999L;
        ProductBatch batchToAdd = createNewProductBatch();

        when(productService.findById(productId)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.addBatchToExistingProduct(productId, batchToAdd, testUser));

        assertEquals("Product with ID 999 does not exist", exception.getMessage());
        verify(productService).findById(productId);
        verify(productBatchRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when batch number already exists for product")
    void testAddBatchToExistingProduct_DuplicateBatchNumber() {
        // Given
        Long productId = 1L;
        ProductBatch batchToAdd = createNewProductBatch();
        batchToAdd.setBatchNumber("EXISTING_BATCH");

        when(productService.findById(productId)).thenReturn(testProduct);
        when(productBatchRepository.existsByProductAndBatchNumber(testProduct, "EXISTING_BATCH")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.addBatchToExistingProduct(productId, batchToAdd, testUser));

        assertTrue(exception.getMessage().contains("already exists for product"));
        verify(productService).findById(productId);
        verify(productBatchRepository).existsByProductAndBatchNumber(testProduct, "EXISTING_BATCH");
        verify(productBatchRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when batch has invalid quantity received")
    void testAddBatchToExistingProduct_InvalidQuantityReceived() {
        // Given
        Long productId = 1L;
        ProductBatch invalidBatch = createNewProductBatch();
        invalidBatch.setQuantityReceived(-5);

        when(productService.findById(productId)).thenReturn(testProduct);
        when(productBatchRepository.existsByProductAndBatchNumber(testProduct, "BATCH002")).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.addBatchToExistingProduct(productId, invalidBatch, testUser));

        assertEquals("Quantity received must be greater than or equal to 0", exception.getMessage());
        verify(productBatchRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when quantity available is greater than quantity received")
    void testAddBatchToExistingProduct_InvalidQuantityAvailable() {
        // Given
        Long productId = 1L;
        ProductBatch invalidBatch = createNewProductBatch();
        invalidBatch.setQuantityReceived(50);
        invalidBatch.setQuantityAvailable(75); // Greater than received

        when(productService.findById(productId)).thenReturn(testProduct);
        when(productBatchRepository.existsByProductAndBatchNumber(testProduct, "BATCH002")).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.addBatchToExistingProduct(productId, invalidBatch, testUser));

        assertEquals("Quantity available cannot be greater than quantity received", exception.getMessage());
        verify(productBatchRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when expiration date is in the past")
    void testAddBatchToExistingProduct_PastExpirationDate() {
        // Given
        Long productId = 1L;
        ProductBatch invalidBatch = createNewProductBatch();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        invalidBatch.setExpirationDate(calendar.getTime());

        when(productService.findById(productId)).thenReturn(testProduct);
        when(productBatchRepository.existsByProductAndBatchNumber(testProduct, "BATCH002")).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.addBatchToExistingProduct(productId, invalidBatch, testUser));

        assertEquals("Expiration date cannot be in the past", exception.getMessage());
        verify(productBatchRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should save product batch successfully")
    void testSave() {
        // Given
        ProductBatch batchToSave = createTestProductBatch();
        ProductBatch savedBatch = createTestProductBatch();
        savedBatch.setBatchId(1);

        when(productBatchRepository.save(batchToSave)).thenReturn(savedBatch);

        // When
        ProductBatch result = service.save(batchToSave);

        // Then
        assertNotNull(result);
        assertEquals(savedBatch, result);
        verify(productBatchRepository).save(batchToSave);
    }

    @Test
    @DisplayName("Should find batches by product")
    void testFindByProduct() {
        // Given
        List<ProductBatch> expectedBatches = Arrays.asList(testBatch);

        when(productBatchRepository.findByProduct(testProduct)).thenReturn(expectedBatches);

        // When
        List<ProductBatch> result = service.findByProduct(testProduct);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedBatches, result);
        verify(productBatchRepository).findByProduct(testProduct);
    }

    @Test
    @DisplayName("Should update batch quantity successfully")
    void testUpdateBatchQuantity() {
        // Given
        Integer batchId = 1;
        Integer quantityChange = -10; // Simulating a sale
        testBatch.setBatchId(batchId);
        testBatch.setQuantityAvailable(50);

        ProductBatch updatedBatch = createTestProductBatch();
        updatedBatch.setBatchId(batchId);
        updatedBatch.setQuantityAvailable(40);

        when(productBatchRepository.findById(batchId)).thenReturn(testBatch);
        when(productBatchRepository.update(any(ProductBatch.class))).thenReturn(updatedBatch);

        // When
        ProductBatch result = service.updateBatchQuantity(batchId, quantityChange);

        // Then
        assertNotNull(result);
        assertEquals(40, result.getQuantityAvailable());

        // Verify the quantity was correctly calculated
        ArgumentCaptor<ProductBatch> batchCaptor = ArgumentCaptor.forClass(ProductBatch.class);
        verify(productBatchRepository).update(batchCaptor.capture());
        ProductBatch capturedBatch = batchCaptor.getValue();
        assertEquals(40, capturedBatch.getQuantityAvailable());

        verify(productBatchRepository).findById(batchId);
    }

    @Test
    @DisplayName("Should throw exception when trying to reduce quantity below zero")
    void testUpdateBatchQuantity_InsufficientQuantity() {
        // Given
        Integer batchId = 1;
        Integer quantityChange = -60; // More than available
        testBatch.setBatchId(batchId);
        testBatch.setQuantityAvailable(50);

        when(productBatchRepository.findById(batchId)).thenReturn(testBatch);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.updateBatchQuantity(batchId, quantityChange));

        assertTrue(exception.getMessage().contains("Insufficient quantity in batch"));
        verify(productBatchRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should check if batch number exists for product")
    void testBatchNumberExists() {
        // Given
        String batchNumber = "BATCH001";

        when(productBatchRepository.existsByProductAndBatchNumber(testProduct, batchNumber)).thenReturn(true);

        // When
        boolean result = service.batchNumberExists(testProduct, batchNumber);

        // Then
        assertTrue(result);
        verify(productBatchRepository).existsByProductAndBatchNumber(testProduct, batchNumber);
    }

    @Test
    @DisplayName("Should find available batches")
    void testFindAvailableBatches() {
        // Given
        List<ProductBatch> expectedBatches = Arrays.asList(testBatch);

        when(productBatchRepository.findAvailableBatches()).thenReturn(expectedBatches);

        // When
        List<ProductBatch> result = service.findAvailableBatches();

        // Then
        assertNotNull(result);
        assertEquals(expectedBatches, result);
        verify(productBatchRepository).findAvailableBatches();
    }

    @Test
    @DisplayName("Should find expiring batches")
    void testFindExpiringBatches() {
        // Given
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 6);
        Date cutoffDate = calendar.getTime();
        List<ProductBatch> expectedBatches = Arrays.asList(testBatch);

        when(productBatchRepository.findByExpirationDateBefore(cutoffDate)).thenReturn(expectedBatches);

        // When
        List<ProductBatch> result = service.findExpiringBatches(cutoffDate);

        // Then
        assertNotNull(result);
        assertEquals(expectedBatches, result);
        verify(productBatchRepository).findByExpirationDateBefore(cutoffDate);
    }

    @Test
    @DisplayName("Should handle empty results gracefully")
    void testFindByProduct_EmptyResult() {
        // Given
        when(productBatchRepository.findByProduct(testProduct)).thenReturn(Collections.emptyList());

        // When
        List<ProductBatch> result = service.findByProduct(testProduct);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productBatchRepository).findByProduct(testProduct);
    }

    /**
     * Helper method to inject mocks using reflection
     */
    private void injectMock(Object target, String fieldName, Object mock)
            throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    /**
     * Helper method to create a test Product
     */
    private Product createTestProduct() {
        ProductType productType = ProductType.builder()
                .typeCode("MED")
                .typeName("Medicine")
                .isActive(true)
                .build();

        ProductCategory category = ProductCategory.builder()
                .categoryCode("ANT")
                .categoryName("Antibiotics")
                .requiresPrescription(true)
                .isActive(true)
                .build();

        return Product.builder()
                .productId(1L)
                .productType(productType)
                .category(category)
                .commercialName("Amoxil")
                .manufacturer("GlaxoSmithKline")
                .isActive(true)
                .build();
    }

    /**
     * Helper method to create a test ProductBatch
     */
    private ProductBatch createTestProductBatch() {
        Calendar calendar = Calendar.getInstance();

        // Manufacture date: January 15, 2024
        calendar.set(2024, Calendar.JANUARY, 15);
        Date manufactureDate = calendar.getTime();

        // Expiration date: January 15, 2026
        calendar.set(2026, Calendar.JANUARY, 15);
        Date expirationDate = calendar.getTime();

        // Received date: February 1, 2024
        calendar.set(2024, Calendar.FEBRUARY, 1);
        Date receivedDate = calendar.getTime();

        return ProductBatch.builder()
                .product(testProduct)
                .batchNumber("BATCH001")
                .quantityReceived(100)
                .quantityAvailable(100)
                .unitCost(new BigDecimal("10.50"))
                .salePrice(new BigDecimal("15.75"))
                .manufactureDate(manufactureDate)
                .expirationDate(expirationDate)
                .receivedDate(receivedDate)
                .isActive(true)
                .isExpired(false)
                .daysUntilExpiration(365)
                .build();
    }

    /**
     * Helper method to create a new ProductBatch for testing add functionality
     */
    private ProductBatch createNewProductBatch() {
        Calendar calendar = Calendar.getInstance();

        // Manufacture date: March 1, 2024
        calendar.set(2024, Calendar.MARCH, 1);
        Date manufactureDate = calendar.getTime();

        // Expiration date: March 1, 2026
        calendar.set(2026, Calendar.MARCH, 1);
        Date expirationDate = calendar.getTime();

        return ProductBatch.builder()
                .batchNumber("BATCH002")
                .quantityReceived(50)
                .quantityAvailable(50)
                .unitCost(new BigDecimal("12.00"))
                .salePrice(new BigDecimal("18.00"))
                .manufactureDate(manufactureDate)
                .expirationDate(expirationDate)
                .isActive(true)
                .isExpired(false)
                .daysUntilExpiration(400)
                .build();
    }

    /**
     * Helper method to create a test User
     */
    private User createTestUser() {
        return User.builder()
                .id("1234567890123")
                .firstName("Admin")
                .lastName("User")
                .userName("adminUser")
                .email("admin@pharmacy.com")
                .phoneNumber("12345678")
                .password("hashedPassword")
                .role(Role.ADMIN)
                .build();
    }
}