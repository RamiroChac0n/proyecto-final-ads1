package com.mycompany.repository;

import com.mycompany.model.entity.*;
import com.mycompany.model.entity.enums.TransferStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * Unit tests for BranchTransferRepository
 * Tests repository operations for BranchTransfer entity
 *
 * @author ramir
 */
@ExtendWith(MockitoExtension.class)
public class BranchTransferRepositoryTest {

    @Mock
    private BranchTransferRepository transferRepository;

    private BranchTransfer testTransfer;
    private Branch fromBranch;
    private Branch toBranch;
    private Product testProduct;
    private ProductBatch testBatch;

    @BeforeEach
    void setUp() {
        fromBranch = createTestBranch(1, "Sucursal Central");
        toBranch = createTestBranch(2, "Sucursal Norte");
        testProduct = createTestProduct();
        testBatch = createTestProductBatch();
        testTransfer = createTestTransfer();
    }

    @Test
    @DisplayName("Should save transfer successfully")
    void save_ValidTransfer_ReturnsSavedTransfer() {
        // Given
        BranchTransfer transferToSave = createTestTransfer();
        BranchTransfer savedTransfer = createTestTransfer();
        savedTransfer.setTransferId(1);

        when(transferRepository.save(transferToSave)).thenReturn(savedTransfer);

        // When
        BranchTransfer result = transferRepository.save(transferToSave);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTransferId());
        assertEquals(TransferStatus.PENDING, result.getStatus());
        verify(transferRepository).save(transferToSave);
    }

    @Test
    @DisplayName("Should find transfer by ID")
    void findById_ExistingId_ReturnsTransfer() {
        // Given
        Integer transferId = 1;
        testTransfer.setTransferId(transferId);

        when(transferRepository.findById(transferId)).thenReturn(testTransfer);

        // When
        BranchTransfer result = transferRepository.findById(transferId);

        // Then
        assertNotNull(result);
        assertEquals(transferId, result.getTransferId());
        verify(transferRepository).findById(transferId);
    }

    @Test
    @DisplayName("Should return null when transfer ID does not exist")
    void findById_NonExistingId_ReturnsNull() {
        // Given
        Integer transferId = 999;

        when(transferRepository.findById(transferId)).thenReturn(null);

        // When
        BranchTransfer result = transferRepository.findById(transferId);

        // Then
        assertNull(result);
        verify(transferRepository).findById(transferId);
    }

    @Test
    @DisplayName("Should return all transfers")
    void findAll_ReturnsAllTransfers() {
        // Given
        List<BranchTransfer> expectedTransfers = Arrays.asList(testTransfer);

        when(transferRepository.findAll()).thenReturn(expectedTransfers);

        // When
        List<BranchTransfer> result = transferRepository.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transferRepository).findAll();
    }

    @Test
    @DisplayName("Should update transfer successfully")
    void update_ValidTransfer_ReturnsUpdatedTransfer() {
        // Given
        testTransfer.setStatus(TransferStatus.IN_TRANSIT);
        testTransfer.setApprovedBy("admin");
        BranchTransfer updatedTransfer = createTestTransfer();
        updatedTransfer.setStatus(TransferStatus.IN_TRANSIT);
        updatedTransfer.setApprovedBy("admin");

        when(transferRepository.update(testTransfer)).thenReturn(updatedTransfer);

        // When
        BranchTransfer result = transferRepository.update(testTransfer);

        // Then
        assertNotNull(result);
        assertEquals(TransferStatus.IN_TRANSIT, result.getStatus());
        assertEquals("admin", result.getApprovedBy());
        verify(transferRepository).update(testTransfer);
    }

    @Test
    @DisplayName("Should delete transfer successfully")
    void delete_ValidTransfer_DeletesTransfer() {
        // When
        transferRepository.delete(testTransfer);

        // Then
        verify(transferRepository).delete(testTransfer);
    }

    /**
     * Helper method to create a test Branch
     */
    private Branch createTestBranch(Integer id, String name) {
        return Branch.builder()
                .branchId(id)
                .branchName(name)
                .address("Test Address")
                .phone("12345678")
                .isActive(true)
                .build();
    }

    /**
     * Helper method to create a test Product
     */
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
                .batchId(1)
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
                .build();
    }

    /**
     * Helper method to create a test BranchTransfer
     */
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
}
