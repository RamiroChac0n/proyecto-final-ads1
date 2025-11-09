package com.mycompany.service.impl;

import com.mycompany.model.entity.*;
import com.mycompany.model.entity.enums.MovementType;
import com.mycompany.model.entity.enums.TransferStatus;
import com.mycompany.repository.BranchTransferRepository;
import com.mycompany.repository.ProductBatchRepository;
import com.mycompany.service.IInventoryMovementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BranchTransferServiceImpl
 * Tests business logic for branch transfers using TDD approach
 *
 * @author ramir
 */
@ExtendWith(MockitoExtension.class)
public class BranchTransferServiceImplTest {

    @Mock
    private BranchTransferRepository transferRepository;

    @Mock
    private ProductBatchRepository batchRepository;

    @Mock
    private IInventoryMovementService inventoryMovementService;

    @InjectMocks
    private BranchTransferServiceImpl transferService;

    private BranchTransfer testTransfer;
    private Branch fromBranch;
    private Branch toBranch;
    private Product testProduct;
    private ProductBatch testBatch;
    private User testUser;

    @BeforeEach
    void setUp() {
        fromBranch = createTestBranch(1, "Sucursal Central");
        toBranch = createTestBranch(2, "Sucursal Norte");
        testProduct = createTestProduct();
        testBatch = createTestProductBatch(100); // 100 available
        testUser = createTestUser();
        testTransfer = createTestTransfer();
    }

    // ==================== REQUEST TRANSFER TESTS ====================

    @Test
    @DisplayName("requestTransfer: Should create transfer with PENDING status")
    void requestTransfer_ValidData_CreatesPendingTransfer() {
        // Given
        BranchTransfer transferToCreate = createTestTransfer();
        transferToCreate.setTransferId(null);

        when(batchRepository.findById(testBatch.getBatchId())).thenReturn(testBatch);
        when(transferRepository.save(any(BranchTransfer.class))).thenAnswer(invocation -> {
            BranchTransfer saved = invocation.getArgument(0);
            saved.setTransferId(1);
            return saved;
        });

        // When
        BranchTransfer result = transferService.requestTransfer(transferToCreate);

        // Then
        assertNotNull(result);
        assertEquals(TransferStatus.PENDING, result.getStatus());
        assertNotNull(result.getTransferId());
        verify(transferRepository).save(any(BranchTransfer.class));
    }

    @Test
    @DisplayName("requestTransfer: Should throw exception when insufficient stock")
    void requestTransfer_InsufficientStock_ThrowsException() {
        // Given
        testBatch.setQuantityAvailable(10); // Only 10 available
        testTransfer.setQuantity(20); // Request 20

        when(batchRepository.findById(testBatch.getBatchId())).thenReturn(testBatch);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transferService.requestTransfer(testTransfer));

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("requestTransfer: Should throw exception when same branch transfer")
    void requestTransfer_SameBranch_ThrowsException() {
        // Given
        testTransfer.setToBranch(fromBranch); // Same as from branch

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transferService.requestTransfer(testTransfer));

        assertTrue(exception.getMessage().contains("Cannot transfer to the same branch"));
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("requestTransfer: Should throw exception when batch is inactive")
    void requestTransfer_InactiveBatch_ThrowsException() {
        // Given
        testBatch.setIsActive(false);

        when(batchRepository.findById(testBatch.getBatchId())).thenReturn(testBatch);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transferService.requestTransfer(testTransfer));

        assertTrue(exception.getMessage().contains("Batch is not active"));
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("requestTransfer: Should throw exception when batch is expired")
    void requestTransfer_ExpiredBatch_ThrowsException() {
        // Given
        testBatch.setIsExpired(true);

        when(batchRepository.findById(testBatch.getBatchId())).thenReturn(testBatch);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transferService.requestTransfer(testTransfer));

        assertTrue(exception.getMessage().contains("Batch is expired"));
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("requestTransfer: Should throw exception when quantity is zero or negative")
    void requestTransfer_ZeroQuantity_ThrowsException() {
        // Given
        testTransfer.setQuantity(0);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transferService.requestTransfer(testTransfer));

        assertTrue(exception.getMessage().contains("Quantity must be positive"));
        verify(transferRepository, never()).save(any());
    }

    // ==================== APPROVE TRANSFER TESTS ====================

    @Test
    @DisplayName("approveTransfer: Should change status from PENDING to IN_TRANSIT")
    void approveTransfer_PendingTransfer_ChangesToInTransit() {
        // Given
        testTransfer.setTransferId(1);
        testTransfer.setStatus(TransferStatus.PENDING);

        when(transferRepository.findById(1)).thenReturn(testTransfer);
        when(batchRepository.findById(testBatch.getBatchId())).thenReturn(testBatch);
        when(transferRepository.update(any(BranchTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BranchTransfer result = transferService.approveTransfer(1, testUser);

        // Then
        assertEquals(TransferStatus.IN_TRANSIT, result.getStatus());
        assertEquals(testUser.getUserName(), result.getApprovedBy());
        verify(transferRepository).update(any(BranchTransfer.class));
    }

    @Test
    @DisplayName("approveTransfer: Should reduce stock in source branch")
    void approveTransfer_ReducesSourceStock() {
        // Given
        testTransfer.setTransferId(1);
        testTransfer.setStatus(TransferStatus.PENDING);
        Integer initialQuantity = testBatch.getQuantityAvailable();

        when(transferRepository.findById(1)).thenReturn(testTransfer);
        when(batchRepository.findById(testBatch.getBatchId())).thenReturn(testBatch);
        when(transferRepository.update(any(BranchTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        transferService.approveTransfer(1, testUser);

        // Then
        assertEquals(initialQuantity - testTransfer.getQuantity(), testBatch.getQuantityAvailable());
        verify(batchRepository).update(testBatch);
    }

    @Test
    @DisplayName("approveTransfer: Should create TRANSFER_OUT inventory movement")
    void approveTransfer_CreatesTransferOutMovement() {
        // Given
        testTransfer.setTransferId(1);
        testTransfer.setStatus(TransferStatus.PENDING);

        when(transferRepository.findById(1)).thenReturn(testTransfer);
        when(batchRepository.findById(testBatch.getBatchId())).thenReturn(testBatch);
        when(transferRepository.update(any(BranchTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        transferService.approveTransfer(1, testUser);

        // Then
        verify(inventoryMovementService).save(argThat(movement ->
                movement.getMovementType() == MovementType.OUT &&
                movement.getQuantity() == -testTransfer.getQuantity() &&
                movement.getProduct().equals(testProduct) &&
                movement.getBatch().equals(testBatch) &&
                movement.getBranch().equals(fromBranch)
        ));
    }

    @Test
    @DisplayName("approveTransfer: Should throw exception when transfer is not PENDING")
    void approveTransfer_NonPendingStatus_ThrowsException() {
        // Given
        testTransfer.setTransferId(1);
        testTransfer.setStatus(TransferStatus.COMPLETED);

        when(transferRepository.findById(1)).thenReturn(testTransfer);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> transferService.approveTransfer(1, testUser));

        assertTrue(exception.getMessage().contains("can only be approved if status is PENDING"));
        verify(batchRepository, never()).update(any());
    }

    @Test
    @DisplayName("approveTransfer: Should throw exception when transfer does not exist")
    void approveTransfer_NonExistentTransfer_ThrowsException() {
        // Given
        when(transferRepository.findById(999)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transferService.approveTransfer(999, testUser));

        assertTrue(exception.getMessage().contains("Transfer not found"));
        verify(batchRepository, never()).update(any());
    }

    @Test
    @DisplayName("approveTransfer: Should throw exception when insufficient stock at approval time")
    void approveTransfer_InsufficientStockAtApproval_ThrowsException() {
        // Given
        testTransfer.setTransferId(1);
        testTransfer.setStatus(TransferStatus.PENDING);
        testTransfer.setQuantity(50);
        testBatch.setQuantityAvailable(30); // Not enough stock anymore

        when(transferRepository.findById(1)).thenReturn(testTransfer);
        when(batchRepository.findById(testBatch.getBatchId())).thenReturn(testBatch);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> transferService.approveTransfer(1, testUser));

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(transferRepository, never()).update(any());
    }

    // ==================== RECEIVE TRANSFER TESTS ====================

    @Test
    @DisplayName("receiveTransfer: Should change status from IN_TRANSIT to COMPLETED")
    void receiveTransfer_InTransitTransfer_ChangesToCompleted() {
        // Given
        testTransfer.setTransferId(1);
        testTransfer.setStatus(TransferStatus.IN_TRANSIT);

        when(transferRepository.findById(1)).thenReturn(testTransfer);
        when(batchRepository.findById(testBatch.getBatchId())).thenReturn(testBatch);
        when(batchRepository.save(any(ProductBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferRepository.update(any(BranchTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BranchTransfer result = transferService.receiveTransfer(1, testUser);

        // Then
        assertEquals(TransferStatus.COMPLETED, result.getStatus());
        assertEquals(testUser.getUserName(), result.getReceivedBy());
        verify(transferRepository).update(any(BranchTransfer.class));
    }

    @Test
    @DisplayName("receiveTransfer: Should increase stock in destination branch")
    void receiveTransfer_IncreasesDestinationStock() {
        // Given
        testTransfer.setTransferId(1);
        testTransfer.setStatus(TransferStatus.IN_TRANSIT);

        when(transferRepository.findById(1)).thenReturn(testTransfer);
        when(batchRepository.findById(testBatch.getBatchId())).thenReturn(testBatch);
        when(batchRepository.save(any(ProductBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferRepository.update(any(BranchTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        transferService.receiveTransfer(1, testUser);

        // Then
        // Verify new batch is created with correct quantity
        verify(batchRepository).save(argThat(batch ->
            batch.getQuantityAvailable().equals(testTransfer.getQuantity()) &&
            batch.getBranch().equals(toBranch)
        ));
    }

    @Test
    @DisplayName("receiveTransfer: Should create TRANSFER_IN inventory movement")
    void receiveTransfer_CreatesTransferInMovement() {
        // Given
        testTransfer.setTransferId(1);
        testTransfer.setStatus(TransferStatus.IN_TRANSIT);

        when(transferRepository.findById(1)).thenReturn(testTransfer);
        when(batchRepository.findById(testBatch.getBatchId())).thenReturn(testBatch);
        when(batchRepository.save(any(ProductBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferRepository.update(any(BranchTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        transferService.receiveTransfer(1, testUser);

        // Then
        verify(inventoryMovementService).save(argThat(movement ->
                movement.getMovementType() == MovementType.IN &&
                movement.getQuantity() == testTransfer.getQuantity() &&
                movement.getProduct().equals(testProduct) &&
                movement.getBranch().equals(toBranch)
        ));
    }

    @Test
    @DisplayName("receiveTransfer: Should throw exception when transfer is not IN_TRANSIT")
    void receiveTransfer_NonInTransitStatus_ThrowsException() {
        // Given
        testTransfer.setTransferId(1);
        testTransfer.setStatus(TransferStatus.PENDING);

        when(transferRepository.findById(1)).thenReturn(testTransfer);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> transferService.receiveTransfer(1, testUser));

        assertTrue(exception.getMessage().contains("can only be received if status is IN_TRANSIT"));
        verify(batchRepository, never()).update(any());
    }

    @Test
    @DisplayName("receiveTransfer: Should always create new batch with unique batch number")
    void receiveTransfer_CreatesNewBatchWithUniqueBatchNumber() {
        // Given
        testTransfer.setTransferId(5);
        testTransfer.setStatus(TransferStatus.IN_TRANSIT);

        when(transferRepository.findById(5)).thenReturn(testTransfer);
        when(batchRepository.findById(testBatch.getBatchId())).thenReturn(testBatch); // Need source batch for copy
        when(batchRepository.save(any(ProductBatch.class))).thenAnswer(invocation -> {
            ProductBatch newBatch = invocation.getArgument(0);
            newBatch.setBatchId(99);
            return newBatch;
        });
        when(transferRepository.update(any(BranchTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        transferService.receiveTransfer(5, testUser);

        // Then
        verify(batchRepository).save(argThat(batch ->
                batch.getProduct().equals(testProduct) &&
                batch.getBatchNumber().equals("BATCH001-T5") && // New unique batch number format
                batch.getQuantityAvailable().equals(testTransfer.getQuantity()) &&
                batch.getBranch().equals(toBranch) // Destination branch
        ));
    }

    // ==================== CANCEL TRANSFER TESTS ====================

    @Test
    @DisplayName("cancelTransfer: Should cancel PENDING transfer")
    void cancelTransfer_PendingStatus_ChangesCancelled() {
        // Given
        testTransfer.setTransferId(1);
        testTransfer.setStatus(TransferStatus.PENDING);
        String reason = "Out of stock at destination";

        when(transferRepository.findById(1)).thenReturn(testTransfer);
        when(transferRepository.update(any(BranchTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BranchTransfer result = transferService.cancelTransfer(1, reason, testUser);

        // Then
        assertEquals(TransferStatus.CANCELLED, result.getStatus());
        assertTrue(result.getNotes().contains(reason));
        assertTrue(result.getNotes().contains("CANCELLED by " + testUser.getUserName()));
        verify(transferRepository).update(any(BranchTransfer.class));
        verify(batchRepository, never()).update(any()); // No inventory to reverse
    }

    @Test
    @DisplayName("cancelTransfer: Should cancel IN_TRANSIT transfer and reverse inventory")
    void cancelTransfer_InTransitStatus_ReversesInventory() {
        // Given
        testTransfer.setTransferId(1);
        testTransfer.setStatus(TransferStatus.IN_TRANSIT);
        String reason = "Product damaged during transit";

        when(transferRepository.findById(1)).thenReturn(testTransfer);
        when(batchRepository.findById(testBatch.getBatchId())).thenReturn(testBatch);
        when(transferRepository.update(any(BranchTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Integer initialQuantity = testBatch.getQuantityAvailable();

        // When
        BranchTransfer result = transferService.cancelTransfer(1, reason, testUser);

        // Then
        assertEquals(TransferStatus.CANCELLED, result.getStatus());
        assertEquals(initialQuantity + testTransfer.getQuantity(), testBatch.getQuantityAvailable());
        verify(batchRepository).update(testBatch);
        verify(inventoryMovementService).save(argThat(movement ->
                movement.getMovementType() == MovementType.IN &&
                movement.getQuantity() == testTransfer.getQuantity()
        ));
    }

    @Test
    @DisplayName("cancelTransfer: Should throw exception when trying to cancel COMPLETED transfer")
    void cancelTransfer_CompletedStatus_ThrowsException() {
        // Given
        testTransfer.setTransferId(1);
        testTransfer.setStatus(TransferStatus.COMPLETED);

        when(transferRepository.findById(1)).thenReturn(testTransfer);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> transferService.cancelTransfer(1, "Some reason", testUser));

        assertTrue(exception.getMessage().contains("Cannot cancel a completed transfer"));
        verify(transferRepository, never()).update(any());
    }

    @Test
    @DisplayName("cancelTransfer: Should throw exception when reason is null or empty")
    void cancelTransfer_RequiresReason() {
        // Given
        testTransfer.setTransferId(1);
        testTransfer.setStatus(TransferStatus.PENDING);

        // When & Then (validation happens before repository call)
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transferService.cancelTransfer(1, null, testUser));

        assertTrue(exception.getMessage().contains("Cancellation reason is required"));
        verify(transferRepository, never()).update(any());
    }

    // ==================== AUXILIARY METHOD TESTS ====================

    @Test
    @DisplayName("validateStockAvailability: Should return true when stock is sufficient")
    void validateStockAvailability_SufficientStock_ReturnsTrue() {
        // Given
        testBatch.setQuantityAvailable(100);
        when(batchRepository.findById(testBatch.getBatchId())).thenReturn(testBatch);

        // When
        boolean result = transferService.validateStockAvailability(testBatch.getBatchId(), 50);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("validateStockAvailability: Should return false when stock is insufficient")
    void validateStockAvailability_InsufficientStock_ReturnsFalse() {
        // Given
        testBatch.setQuantityAvailable(30);
        when(batchRepository.findById(testBatch.getBatchId())).thenReturn(testBatch);

        // When
        boolean result = transferService.validateStockAvailability(testBatch.getBatchId(), 50);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("list: Should return all transfers")
    void list_ReturnsAllTransfers() {
        // Given
        List<BranchTransfer> expectedTransfers = Arrays.asList(testTransfer);
        when(transferRepository.findAll()).thenReturn(expectedTransfers);

        // When
        List<BranchTransfer> result = transferService.list();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transferRepository).findAll();
    }

    @Test
    @DisplayName("findById: Should return transfer when exists")
    void findById_ExistingId_ReturnsTransfer() {
        // Given
        testTransfer.setTransferId(1);
        when(transferRepository.findById(1)).thenReturn(testTransfer);

        // When
        BranchTransfer result = transferService.findById(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTransferId());
        verify(transferRepository).findById(1);
    }

    // ==================== HELPER METHODS ====================

    private Branch createTestBranch(Integer id, String name) {
        return Branch.builder()
                .branchId(id)
                .branchName(name)
                .address("Test Address")
                .phone("12345678")
                .isActive(true)
                .build();
    }

    private Product createTestProduct() {
        return Product.builder()
                .productId(1L)
                .commercialName("Paracetamol 500mg")
                .manufacturer("Test Pharma")
                .isActive(true)
                .minStock(10)
                .maxStock(100)
                .build();
    }

    private ProductBatch createTestProductBatch(int quantityAvailable) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(2024, Calendar.JANUARY, 15);
        Date manufactureDate = calendar.getTime();

        calendar.set(2026, Calendar.JANUARY, 15);
        Date expirationDate = calendar.getTime();

        calendar.set(2024, Calendar.FEBRUARY, 1);
        Date receivedDate = calendar.getTime();

        return ProductBatch.builder()
                .batchId(1)
                .product(testProduct)
                .batchNumber("BATCH001")
                .quantityReceived(quantityAvailable)
                .quantityAvailable(quantityAvailable)
                .unitCost(new BigDecimal("10.50"))
                .salePrice(new BigDecimal("15.75"))
                .manufactureDate(manufactureDate)
                .expirationDate(expirationDate)
                .receivedDate(receivedDate)
                .isActive(true)
                .isExpired(false)
                .build();
    }

    private BranchTransfer createTestTransfer() {
        return BranchTransfer.builder()
                .product(testProduct)
                .batch(testBatch)
                .fromBranch(fromBranch)
                .toBranch(toBranch)
                .quantity(20)
                .status(TransferStatus.PENDING)
                .requestedBy("admin")
                .notes("Test transfer")
                .transferDate(new Date())
                .build();
    }

    private User createTestUser() {
        User user = new User();
        user.setId("admin123");
        user.setUserName("admin");
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setBranch(toBranch);  // User belongs to destination branch for receive tests
        return user;
    }
}
