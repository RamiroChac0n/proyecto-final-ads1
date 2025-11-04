package com.mycompany.service.impl;

import com.mycompany.model.entity.*;
import com.mycompany.model.entity.enums.MovementType;
import com.mycompany.model.entity.enums.TransferStatus;
import com.mycompany.repository.BranchTransferRepository;
import com.mycompany.repository.ProductBatchRepository;
import com.mycompany.service.IBranchTransferService;
import com.mycompany.service.IInventoryMovementService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Service implementation for branch transfer operations
 * Implements business logic for managing product transfers between branches
 *
 * @author ramir
 */
@Stateless
public class BranchTransferServiceImpl implements IBranchTransferService {

    @EJB
    private BranchTransferRepository transferRepository;

    @EJB
    private ProductBatchRepository batchRepository;

    @EJB
    private IInventoryMovementService inventoryMovementService;

    @Override
    public BranchTransfer requestTransfer(BranchTransfer transfer) {
        // Validate quantity is positive
        if (transfer.getQuantity() == null || transfer.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Validate not transferring to same branch
        if (transfer.getFromBranch().getBranchId().equals(transfer.getToBranch().getBranchId())) {
            throw new IllegalArgumentException("Cannot transfer to the same branch");
        }

        // Validate batch exists and is active
        ProductBatch batch = batchRepository.findById(transfer.getBatch().getBatchId());
        if (batch == null) {
            throw new IllegalArgumentException("Batch not found");
        }

        if (!batch.getIsActive()) {
            throw new IllegalArgumentException("Batch is not active");
        }

        if (batch.getIsExpired()) {
            throw new IllegalArgumentException("Batch is expired");
        }

        // Validate sufficient stock
        if (batch.getQuantityAvailable() < transfer.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock in batch. Available: " + batch.getQuantityAvailable());
        }

        // Set initial status
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setTransferDate(new Date());

        // Save transfer
        return transferRepository.save(transfer);
    }

    @Override
    public BranchTransfer approveTransfer(Integer transferId, User approvedBy) {
        // Find transfer
        BranchTransfer transfer = transferRepository.findById(transferId);
        if (transfer == null) {
            throw new IllegalArgumentException("Transfer not found with ID: " + transferId);
        }

        // Validate status is PENDING
        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new IllegalStateException("Transfer can only be approved if status is PENDING. Current status: " + transfer.getStatus());
        }

        // Get batch and validate stock still available
        ProductBatch batch = batchRepository.findById(transfer.getBatch().getBatchId());
        if (batch == null) {
            throw new IllegalArgumentException("Batch not found");
        }

        if (batch.getQuantityAvailable() < transfer.getQuantity()) {
            throw new IllegalStateException("Insufficient stock in batch. Available: " + batch.getQuantityAvailable());
        }

        // Reduce stock at source branch
        batch.setQuantityAvailable(batch.getQuantityAvailable() - transfer.getQuantity());
        batchRepository.update(batch);

        // Create TRANSFER_OUT inventory movement
        InventoryMovement movementOut = InventoryMovement.builder()
                .product(transfer.getProduct())
                .batch(batch)
                .branch(transfer.getFromBranch())
                .movementType(MovementType.OUT)
                .quantity(-transfer.getQuantity())
                .movementDate(LocalDateTime.now())
                .reason("Transfer to " + transfer.getToBranch().getBranchName() + " - Transfer ID: " + transferId)
                .user(approvedBy)
                .build();
        inventoryMovementService.save(movementOut);

        // Update transfer status
        transfer.setStatus(TransferStatus.IN_TRANSIT);
        transfer.setApprovedBy(approvedBy.getUserName());

        return transferRepository.update(transfer);
    }

    @Override
    public BranchTransfer receiveTransfer(Integer transferId, User receivedBy) {
        // Find transfer
        BranchTransfer transfer = transferRepository.findById(transferId);
        if (transfer == null) {
            throw new IllegalArgumentException("Transfer not found with ID: " + transferId);
        }

        // Validate status is IN_TRANSIT
        if (transfer.getStatus() != TransferStatus.IN_TRANSIT) {
            throw new IllegalStateException("Transfer can only be received if status is IN_TRANSIT. Current status: " + transfer.getStatus());
        }

        // Always create a new batch at destination for complete traceability
        ProductBatch sourceBatch = batchRepository.findById(transfer.getBatch().getBatchId());
        if (sourceBatch == null) {
            throw new IllegalArgumentException("Source batch not found");
        }

        // Generate unique batch number for the transfer
        String newBatchNumber = sourceBatch.getBatchNumber() + "-T" + transferId;

        // Create new batch at destination branch
        ProductBatch destinationBatch = ProductBatch.builder()
                .product(transfer.getProduct())
                .batchNumber(newBatchNumber)
                .quantityReceived(transfer.getQuantity())
                .quantityAvailable(transfer.getQuantity())
                .unitCost(sourceBatch.getUnitCost())
                .salePrice(sourceBatch.getSalePrice())
                .manufactureDate(sourceBatch.getManufactureDate())
                .expirationDate(sourceBatch.getExpirationDate())
                .receivedDate(new Date())
                .isActive(true)
                .isExpired(false)
                .branch(transfer.getToBranch())
                .build();

        destinationBatch = batchRepository.save(destinationBatch);

        // Create TRANSFER_IN inventory movement
        InventoryMovement movementIn = InventoryMovement.builder()
                .product(transfer.getProduct())
                .batch(destinationBatch)
                .branch(transfer.getToBranch())
                .movementType(MovementType.IN)
                .quantity(transfer.getQuantity())
                .movementDate(LocalDateTime.now())
                .reason("Transfer from " + transfer.getFromBranch().getBranchName() +
                        " - Transfer ID: " + transferId +
                        " - Original batch: " + sourceBatch.getBatchNumber())
                .user(receivedBy)
                .build();
        inventoryMovementService.save(movementIn);

        // Update transfer status
        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setReceivedBy(receivedBy.getUserName());

        return transferRepository.update(transfer);
    }

    @Override
    public BranchTransfer cancelTransfer(Integer transferId, String reason, User cancelledBy) {
        // Validate reason is provided
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }

        // Find transfer
        BranchTransfer transfer = transferRepository.findById(transferId);
        if (transfer == null) {
            throw new IllegalArgumentException("Transfer not found with ID: " + transferId);
        }

        // Cannot cancel completed transfers
        if (transfer.getStatus() == TransferStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed transfer");
        }

        // If IN_TRANSIT, reverse the stock reduction at source
        if (transfer.getStatus() == TransferStatus.IN_TRANSIT) {
            ProductBatch batch = batchRepository.findById(transfer.getBatch().getBatchId());
            if (batch != null) {
                // Add stock back to source branch
                batch.setQuantityAvailable(batch.getQuantityAvailable() + transfer.getQuantity());
                batchRepository.update(batch);

                // Create reversal inventory movement
                InventoryMovement reversalMovement = InventoryMovement.builder()
                        .product(transfer.getProduct())
                        .batch(batch)
                        .branch(transfer.getFromBranch())
                        .movementType(MovementType.IN)
                        .quantity(transfer.getQuantity())
                        .movementDate(LocalDateTime.now())
                        .reason("Transfer cancelled - Stock returned. Transfer ID: " + transferId + ". Reason: " + reason)
                        .user(cancelledBy)
                        .build();
                inventoryMovementService.save(reversalMovement);
            }
        }

        // Update transfer status
        transfer.setStatus(TransferStatus.CANCELLED);
        transfer.setNotes((transfer.getNotes() != null ? transfer.getNotes() + "\n" : "") +
                "CANCELLED by " + cancelledBy.getUserName() + ": " + reason);

        return transferRepository.update(transfer);
    }

    @Override
    public boolean validateStockAvailability(Integer batchId, Integer quantity) {
        ProductBatch batch = batchRepository.findById(batchId);
        if (batch == null) {
            return false;
        }
        return batch.getQuantityAvailable() >= quantity;
    }

    @Override
    public BranchTransfer save(BranchTransfer transfer) {
        return transferRepository.save(transfer);
    }

    @Override
    public BranchTransfer edit(BranchTransfer transfer) {
        return transferRepository.update(transfer);
    }

    @Override
    public void delete(BranchTransfer transfer) {
        transferRepository.delete(transfer);
    }

    @Override
    public List<BranchTransfer> list() {
        return transferRepository.findAll();
    }

    @Override
    public BranchTransfer findById(Integer id) {
        return transferRepository.findById(id);
    }
}
