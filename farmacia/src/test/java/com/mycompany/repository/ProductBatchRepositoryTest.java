package com.mycompany.repository;

import com.mycompany.model.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductBatchRepository
 * Tests repository operations for ProductBatch entity
 * @author ramir
 */
@ExtendWith(MockitoExtension.class)
public class ProductBatchRepositoryTest {

    @Mock
    private ProductBatchRepository productBatchRepository;

    private Product testProduct;
    private ProductBatch testBatch;

    @BeforeEach
    void setUp() {
        testProduct = createTestProduct();
        testBatch = createTestProductBatch();
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
        ProductBatch result = productBatchRepository.save(batchToSave);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getBatchId());
        assertEquals("BATCH001", result.getBatchNumber());
        verify(productBatchRepository).save(batchToSave);
    }

    @Test
    @DisplayName("Should find batches by product")
    void testFindByProduct() {
        // Given
        List<ProductBatch> expectedBatches = Arrays.asList(testBatch);

        when(productBatchRepository.findByProduct(testProduct)).thenReturn(expectedBatches);

        // When
        List<ProductBatch> result = productBatchRepository.findByProduct(testProduct);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBatch, result.get(0));
        verify(productBatchRepository).findByProduct(testProduct);
    }

    @Test
    @DisplayName("Should find batch by product and batch number")
    void testFindByProductAndBatchNumber() {
        // Given
        String batchNumber = "BATCH001";

        when(productBatchRepository.findByProductAndBatchNumber(testProduct, batchNumber))
                .thenReturn(testBatch);

        // When
        ProductBatch result = productBatchRepository.findByProductAndBatchNumber(testProduct, batchNumber);

        // Then
        assertNotNull(result);
        assertEquals(testBatch, result);
        assertEquals(batchNumber, result.getBatchNumber());
        verify(productBatchRepository).findByProductAndBatchNumber(testProduct, batchNumber);
    }

    @Test
    @DisplayName("Should return null when batch not found by product and batch number")
    void testFindByProductAndBatchNumber_NotFound() {
        // Given
        String batchNumber = "NONEXISTENT";

        when(productBatchRepository.findByProductAndBatchNumber(testProduct, batchNumber))
                .thenReturn(null);

        // When
        ProductBatch result = productBatchRepository.findByProductAndBatchNumber(testProduct, batchNumber);

        // Then
        assertNull(result);
        verify(productBatchRepository).findByProductAndBatchNumber(testProduct, batchNumber);
    }

    @Test
    @DisplayName("Should find available batches")
    void testFindAvailableBatches() {
        // Given
        ProductBatch availableBatch = createTestProductBatch();
        availableBatch.setQuantityAvailable(50);
        List<ProductBatch> expectedBatches = Arrays.asList(availableBatch);

        when(productBatchRepository.findAvailableBatches()).thenReturn(expectedBatches);

        // When
        List<ProductBatch> result = productBatchRepository.findAvailableBatches();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getQuantityAvailable() > 0);
        verify(productBatchRepository).findAvailableBatches();
    }

    @Test
    @DisplayName("Should find batches expiring before date")
    void testFindByExpirationDateBefore() {
        // Given
        LocalDate cutoffDate = LocalDate.now().plusMonths(6);
        ProductBatch expiringBatch = createTestProductBatch();
        expiringBatch.setExpirationDate(LocalDate.now().plusMonths(3));
        List<ProductBatch> expectedBatches = Arrays.asList(expiringBatch);

        when(productBatchRepository.findByExpirationDateBefore(cutoffDate)).thenReturn(expectedBatches);

        // When
        List<ProductBatch> result = productBatchRepository.findByExpirationDateBefore(cutoffDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getExpirationDate().isBefore(cutoffDate));
        verify(productBatchRepository).findByExpirationDateBefore(cutoffDate);
    }

    @Test
    @DisplayName("Should find active batches by product")
    void testFindActiveByProduct() {
        // Given
        ProductBatch activeBatch = createTestProductBatch();
        activeBatch.setIsActive(true);
        List<ProductBatch> expectedBatches = Arrays.asList(activeBatch);

        when(productBatchRepository.findActiveByProduct(testProduct)).thenReturn(expectedBatches);

        // When
        List<ProductBatch> result = productBatchRepository.findActiveByProduct(testProduct);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsActive());
        verify(productBatchRepository).findActiveByProduct(testProduct);
    }

    @Test
    @DisplayName("Should check if batch number exists for product")
    void testExistsByProductAndBatchNumber_True() {
        // Given
        String batchNumber = "BATCH001";

        when(productBatchRepository.existsByProductAndBatchNumber(testProduct, batchNumber))
                .thenReturn(true);

        // When
        boolean result = productBatchRepository.existsByProductAndBatchNumber(testProduct, batchNumber);

        // Then
        assertTrue(result);
        verify(productBatchRepository).existsByProductAndBatchNumber(testProduct, batchNumber);
    }

    @Test
    @DisplayName("Should return false when batch number does not exist for product")
    void testExistsByProductAndBatchNumber_False() {
        // Given
        String batchNumber = "NEWBATCH";

        when(productBatchRepository.existsByProductAndBatchNumber(testProduct, batchNumber))
                .thenReturn(false);

        // When
        boolean result = productBatchRepository.existsByProductAndBatchNumber(testProduct, batchNumber);

        // Then
        assertFalse(result);
        verify(productBatchRepository).existsByProductAndBatchNumber(testProduct, batchNumber);
    }

    @Test
    @DisplayName("Should update product batch successfully")
    void testUpdate() {
        // Given
        testBatch.setQuantityAvailable(75);
        ProductBatch updatedBatch = createTestProductBatch();
        updatedBatch.setQuantityAvailable(75);

        when(productBatchRepository.update(testBatch)).thenReturn(updatedBatch);

        // When
        ProductBatch result = productBatchRepository.update(testBatch);

        // Then
        assertNotNull(result);
        assertEquals(75, result.getQuantityAvailable());
        verify(productBatchRepository).update(testBatch);
    }

    @Test
    @DisplayName("Should delete product batch successfully")
    void testDelete() {
        // When
        productBatchRepository.delete(testBatch);

        // Then
        verify(productBatchRepository).delete(testBatch);
    }

    @Test
    @DisplayName("Should find batch by ID")
    void testFindById() {
        // Given
        Integer batchId = 1;

        when(productBatchRepository.findById(batchId)).thenReturn(testBatch);

        // When
        ProductBatch result = productBatchRepository.findById(batchId);

        // Then
        assertNotNull(result);
        assertEquals(testBatch, result);
        verify(productBatchRepository).findById(batchId);
    }

    /**
     * Helper method to create a test Product
     */
    private Product createTestProduct() {
        return Product.builder()
                .productId(1L)
                .commercialName("Test Product")
                .manufacturer("Test Manufacturer")
                .isActive(true)
                .build();
    }

    /**
     * Helper method to create a test ProductBatch
     */
    private ProductBatch createTestProductBatch() {
        return ProductBatch.builder()
                .product(testProduct)
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
    }
}