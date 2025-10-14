package com.mycompany.service.impl;

import com.mycompany.model.dto.BatchAllocation;
import com.mycompany.model.entity.*;
import com.mycompany.model.entity.enums.MovementType;
import com.mycompany.repository.ProductBatchRepository;
import com.mycompany.service.IProductBatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TDD Tests for SaleServiceImpl
 * Following TDD methodology: tests first, implementation later
 *
 * Tests focus on inventory allocation with FIFO (First In, First Out / PEPS) strategy
 * @author ramir
 */
@ExtendWith(MockitoExtension.class)
public class SaleServiceImplTest {

    private SaleServiceImpl service;

    @Mock
    private ProductBatchRepository productBatchRepository;

    @Mock
    private IProductBatchService productBatchService;

    private Product testProduct;
    private List<ProductBatch> testBatches;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        service = new SaleServiceImpl();

        // Inject mock dependencies using reflection
        injectMock(service, "productBatchRepository", productBatchRepository);
        injectMock(service, "productBatchService", productBatchService);

        testProduct = createTestProduct();
        testBatches = new ArrayList<>();
    }

    /**
     * TEST 1: Salida con un solo lote
     * Verifica que cuando hay stock suficiente en un solo lote,
     * se asigna correctamente y se reduce la cantidad disponible
     */
    @Test
    @DisplayName("Test 1: Should allocate stock from single batch when sufficient quantity available")
    void testAllocateStock_SingleBatch_SufficientQuantity() {
        // Given: Un lote con 100 unidades disponibles
        ProductBatch batch = createBatch(1, "BATCH001", 100, 100,
            new Date(2024, 0, 15), // manufactureDate
            new Date(2026, 0, 15)  // expirationDate
        );
        testBatches.add(batch);

        Integer requestedQuantity = 50;

        when(productBatchRepository.findAvailableBatchesByProductFIFO(testProduct))
            .thenReturn(testBatches);

        // When: Se solicita asignar 50 unidades
        List<BatchAllocation> allocations = service.allocateStock(testProduct, requestedQuantity);

        // Then: Debe retornar 1 asignación del lote único
        assertNotNull(allocations, "Allocations should not be null");
        assertEquals(1, allocations.size(), "Should allocate from single batch");

        BatchAllocation allocation = allocations.get(0);
        assertEquals(1, allocation.getBatchId(), "Should be batch 1");
        assertEquals(50, allocation.getQuantity(), "Should allocate 50 units");
        assertEquals("BATCH001", allocation.getBatchNumber());

        // Verify repository was called
        verify(productBatchRepository).findAvailableBatchesByProductFIFO(testProduct);
    }

    /**
     * TEST 2: Salida que requiere múltiples lotes
     * Verifica que cuando la cantidad solicitada excede el stock de un lote,
     * se utilizan múltiples lotes siguiendo orden FIFO
     */
    @Test
    @DisplayName("Test 2: Should allocate stock from multiple batches using FIFO when single batch insufficient")
    void testAllocateStock_MultipleBatches_FIFO() {
        // Given: 3 lotes con diferentes cantidades
        ProductBatch batch1 = createBatch(1, "BATCH001", 30, 30,
            new Date(2024, 0, 1),
            new Date(2025, 5, 1)  // Expira primero (Junio 2025)
        );
        ProductBatch batch2 = createBatch(2, "BATCH002", 40, 40,
            new Date(2024, 1, 1),
            new Date(2025, 8, 1)  // Expira segundo (Septiembre 2025)
        );
        ProductBatch batch3 = createBatch(3, "BATCH003", 50, 50,
            new Date(2024, 2, 1),
            new Date(2025, 11, 1) // Expira último (Diciembre 2025)
        );

        testBatches.add(batch1);
        testBatches.add(batch2);
        testBatches.add(batch3);

        Integer requestedQuantity = 80; // Necesita batch1 (30) + batch2 (40) + parte de batch3 (10)

        when(productBatchRepository.findAvailableBatchesByProductFIFO(testProduct))
            .thenReturn(testBatches);

        // When: Se solicitan 80 unidades
        List<BatchAllocation> allocations = service.allocateStock(testProduct, requestedQuantity);

        // Then: Debe usar 3 lotes en orden FIFO
        assertNotNull(allocations);
        assertEquals(3, allocations.size(), "Should allocate from 3 batches");

        // Verificar primer lote (más antiguo)
        assertEquals(1, allocations.get(0).getBatchId());
        assertEquals(30, allocations.get(0).getQuantity(), "Should take all 30 from batch1");

        // Verificar segundo lote
        assertEquals(2, allocations.get(1).getBatchId());
        assertEquals(40, allocations.get(1).getQuantity(), "Should take all 40 from batch2");

        // Verificar tercer lote (solo lo necesario)
        assertEquals(3, allocations.get(2).getBatchId());
        assertEquals(10, allocations.get(2).getQuantity(), "Should take only 10 from batch3");

        verify(productBatchRepository).findAvailableBatchesByProductFIFO(testProduct);
    }

    /**
     * TEST 3: Salida con cantidad exacta disponible
     * Verifica que cuando se solicita exactamente la cantidad disponible en un lote,
     * la asignación es correcta y el lote queda en cero
     */
    @Test
    @DisplayName("Test 3: Should allocate exact available quantity leaving batch at zero")
    void testAllocateStock_ExactQuantityAvailable() {
        // Given: Un lote con exactamente 75 unidades
        ProductBatch batch = createBatch(1, "BATCH001", 75, 75,
            new Date(2024, 0, 15),
            new Date(2026, 0, 15)
        );
        testBatches.add(batch);

        Integer requestedQuantity = 75; // Exactamente la cantidad disponible

        when(productBatchRepository.findAvailableBatchesByProductFIFO(testProduct))
            .thenReturn(testBatches);

        // When: Se solicitan exactamente 75 unidades
        List<BatchAllocation> allocations = service.allocateStock(testProduct, requestedQuantity);

        // Then: Debe asignar las 75 unidades del único lote
        assertNotNull(allocations);
        assertEquals(1, allocations.size());
        assertEquals(75, allocations.get(0).getQuantity(), "Should allocate all 75 units");
        assertEquals(1, allocations.get(0).getBatchId());

        verify(productBatchRepository).findAvailableBatchesByProductFIFO(testProduct);
    }

    /**
     * TEST 4: Intento de salida sin stock suficiente
     * Verifica que cuando no hay stock suficiente en ningún lote,
     * se lanza una excepción apropiada sin modificar el inventario
     */
    @Test
    @DisplayName("Test 4: Should throw exception when insufficient stock across all batches")
    void testAllocateStock_InsufficientStock_ThrowsException() {
        // Given: Lotes con stock total insuficiente
        ProductBatch batch1 = createBatch(1, "BATCH001", 20, 20,
            new Date(2024, 0, 1),
            new Date(2025, 5, 1)
        );
        ProductBatch batch2 = createBatch(2, "BATCH002", 25, 25,
            new Date(2024, 1, 1),
            new Date(2025, 8, 1)
        );
        testBatches.add(batch1);
        testBatches.add(batch2);

        Integer requestedQuantity = 100; // Se solicitan 100 pero solo hay 45 total

        when(productBatchRepository.findAvailableBatchesByProductFIFO(testProduct))
            .thenReturn(testBatches);

        // When & Then: Debe lanzar excepción
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.allocateStock(testProduct, requestedQuantity),
            "Should throw exception for insufficient stock"
        );

        assertTrue(exception.getMessage().contains("Insufficient stock"),
            "Exception message should mention insufficient stock");
        assertTrue(exception.getMessage().contains("100"),
            "Exception should mention requested quantity");
        assertTrue(exception.getMessage().contains("45"),
            "Exception should mention available quantity");

        verify(productBatchRepository).findAvailableBatchesByProductFIFO(testProduct);
        // Verify no batch quantities were modified
        verify(productBatchService, never()).updateBatchQuantity(anyInt(), anyInt());
    }

    /**
     * TEST 5: Actualización correcta de quantities
     * Verifica que después de una asignación, las cantidades se actualizan correctamente
     * y se crean los movimientos de inventario correspondientes
     */
    @Test
    @DisplayName("Test 5: Should correctly update batch quantities and create inventory movements")
    void testAllocateStock_CorrectQuantityUpdates() {
        // Given: 2 lotes disponibles
        ProductBatch batch1 = createBatch(1, "BATCH001", 60, 60,
            new Date(2024, 0, 1),
            new Date(2025, 5, 1)
        );
        ProductBatch batch2 = createBatch(2, "BATCH002", 80, 80,
            new Date(2024, 1, 1),
            new Date(2025, 8, 1)
        );
        testBatches.add(batch1);
        testBatches.add(batch2);

        Integer requestedQuantity = 100; // Necesita todo batch1 (60) + 40 de batch2

        when(productBatchRepository.findAvailableBatchesByProductFIFO(testProduct))
            .thenReturn(testBatches);

        // Mock the quantity update behavior
        when(productBatchService.updateBatchQuantity(eq(1), eq(-60)))
            .thenReturn(batch1);
        when(productBatchService.updateBatchQuantity(eq(2), eq(-40)))
            .thenReturn(batch2);

        // When: Se procesan las asignaciones
        List<BatchAllocation> allocations = service.allocateStock(testProduct, requestedQuantity);

        // Simulate applying the allocations (this would normally be done in processSale)
        for (BatchAllocation allocation : allocations) {
            service.applyBatchAllocation(allocation);
        }

        // Then: Verify quantities were updated correctly
        verify(productBatchService).updateBatchQuantity(1, -60);
        verify(productBatchService).updateBatchQuantity(2, -40);

        // Verify allocations are correct
        assertEquals(2, allocations.size());
        assertEquals(60, allocations.get(0).getQuantity());
        assertEquals(40, allocations.get(1).getQuantity());
    }

    /**
     * TEST 6: Respeto del orden FIFO
     * Verifica que los lotes se consumen estrictamente en orden de fecha de expiración
     * (First Expire, First Out - FEFO, que es la implementación común de FIFO en farmacias)
     */
    @Test
    @DisplayName("Test 6: Should strictly respect FIFO order based on expiration date")
    void testAllocateStock_StrictFIFOOrder() {
        // Given: 4 lotes con fechas de expiración desordenadas
        ProductBatch batch1 = createBatch(1, "BATCH001", 25, 25,
            new Date(2024, 0, 10),
            new Date(2025, 11, 15) // Expira cuarto (Diciembre 2025)
        );
        ProductBatch batch2 = createBatch(2, "BATCH002", 30, 30,
            new Date(2024, 0, 5),
            new Date(2025, 2, 10)  // Expira primero (Marzo 2025)
        );
        ProductBatch batch3 = createBatch(3, "BATCH003", 35, 35,
            new Date(2024, 0, 20),
            new Date(2025, 6, 20)  // Expira segundo (Julio 2025)
        );
        ProductBatch batch4 = createBatch(4, "BATCH004", 40, 40,
            new Date(2024, 0, 25),
            new Date(2025, 9, 5)   // Expira tercero (Octubre 2025)
        );

        // El repository debe devolver los lotes YA ORDENADOS por FIFO
        // (esto simula que el repository hace ORDER BY expiration_date ASC)
        testBatches.add(batch2); // Expira primero
        testBatches.add(batch3); // Expira segundo
        testBatches.add(batch4); // Expira tercero
        testBatches.add(batch1); // Expira cuarto

        Integer requestedQuantity = 70; // Debe tomar batch2 (30) + batch3 (35) + parte de batch4 (5)

        when(productBatchRepository.findAvailableBatchesByProductFIFO(testProduct))
            .thenReturn(testBatches);

        // When: Se solicitan 70 unidades
        List<BatchAllocation> allocations = service.allocateStock(testProduct, requestedQuantity);

        // Then: Debe respetar orden FIFO estrictamente
        assertNotNull(allocations);
        assertEquals(3, allocations.size(), "Should use 3 batches in FIFO order");

        // Primer lote consumido: batch2 (expira primero - Marzo 2025)
        assertEquals(2, allocations.get(0).getBatchId(),
            "First allocation should be from batch2 (earliest expiration)");
        assertEquals(30, allocations.get(0).getQuantity());

        // Segundo lote consumido: batch3 (expira segundo - Julio 2025)
        assertEquals(3, allocations.get(1).getBatchId(),
            "Second allocation should be from batch3 (second earliest expiration)");
        assertEquals(35, allocations.get(1).getQuantity());

        // Tercer lote consumido parcialmente: batch4 (expira tercero - Octubre 2025)
        assertEquals(4, allocations.get(2).getBatchId(),
            "Third allocation should be from batch4 (third earliest expiration)");
        assertEquals(5, allocations.get(2).getQuantity(),
            "Should only take 5 units from batch4");

        // Batch1 NO debe ser usado (expira último y no es necesario)
        assertTrue(allocations.stream().noneMatch(a -> a.getBatchId() == 1),
            "Batch1 should not be used as it expires last");

        verify(productBatchRepository).findAvailableBatchesByProductFIFO(testProduct);
    }

    /**
     * TEST ADICIONAL: Validación de disponibilidad de stock
     * Verifica el método validateStockAvailability
     */
    @Test
    @DisplayName("Should validate stock availability correctly")
    void testValidateStockAvailability() {
        // Given: 2 lotes con 50 unidades cada uno (total 100)
        ProductBatch batch1 = createBatch(1, "BATCH001", 50, 50,
            new Date(2024, 0, 1), new Date(2025, 5, 1));
        ProductBatch batch2 = createBatch(2, "BATCH002", 50, 50,
            new Date(2024, 1, 1), new Date(2025, 8, 1));
        testBatches.add(batch1);
        testBatches.add(batch2);

        when(productBatchRepository.findAvailableBatchesByProductFIFO(testProduct))
            .thenReturn(testBatches);

        // When & Then: Stock suficiente
        assertTrue(service.validateStockAvailability(testProduct, 80),
            "Should return true when stock is sufficient");

        // When & Then: Stock exacto
        assertTrue(service.validateStockAvailability(testProduct, 100),
            "Should return true when requesting exact available quantity");

        // When & Then: Stock insuficiente
        assertFalse(service.validateStockAvailability(testProduct, 101),
            "Should return false when stock is insufficient");
    }

    // ==================== HELPER METHODS ====================

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

        ActivePrinciple principle = ActivePrinciple.builder()
                .principleCode("AMX")
                .innName("Amoxicillin")
                .requiresPrescription(true)
                .build();

        DosageForm dosageForm = DosageForm.builder()
                .formCode("TAB")
                .formName("Tablet")
                .routeAdministration("Oral")
                .build();

        ConcentrationUnit unit = ConcentrationUnit.builder()
                .unitCode("MG")
                .unitName("Milligrams")
                .build();

        return Product.builder()
                .productId(1L)
                .productType(productType)
                .category(category)
                .activePrinciple(principle)
                .concentration("500")
                .concentrationUnit(unit)
                .dosageForm(dosageForm)
                .commercialName("Amoxil 500mg")
                .manufacturer("GlaxoSmithKline")
                .requiresPrescription(true)
                .minStock(10)
                .maxStock(500)
                .isActive(true)
                .build();
    }

    /**
     * Helper method to create a ProductBatch for testing
     */
    private ProductBatch createBatch(Integer batchId, String batchNumber,
                                    Integer quantityReceived, Integer quantityAvailable,
                                    Date manufactureDate, Date expirationDate) {
        return ProductBatch.builder()
                .batchId(batchId)
                .product(testProduct)
                .batchNumber(batchNumber)
                .quantityReceived(quantityReceived)
                .quantityAvailable(quantityAvailable)
                .unitCost(new BigDecimal("10.50"))
                .salePrice(new BigDecimal("15.75"))
                .manufactureDate(manufactureDate)
                .expirationDate(expirationDate)
                .receivedDate(new Date())
                .isActive(true)
                .isExpired(false)
                .build();
    }
}
