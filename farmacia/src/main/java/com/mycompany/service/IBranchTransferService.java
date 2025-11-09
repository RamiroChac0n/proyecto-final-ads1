package com.mycompany.service;

import com.mycompany.model.entity.BranchTransfer;
import com.mycompany.model.entity.User;
import jakarta.ejb.Local;

import java.util.List;

/**
 * Service interface for branch transfer operations
 * Defines business logic for managing product transfers between branches
 *
 * @author ramir
 */
@Local
public interface IBranchTransferService {

    /**
     * Request a new transfer between branches
     * Creates a transfer with PENDING status after validating business rules
     *
     * @param transfer The transfer to request
     * @return The created transfer with PENDING status
     * @throws IllegalArgumentException if validation fails (insufficient stock, same branch, invalid quantity, etc.)
     */
    BranchTransfer requestTransfer(BranchTransfer transfer);

    /**
     * Approve a pending transfer
     * Changes status from PENDING to IN_TRANSIT, reduces stock at source branch,
     * and creates TRANSFER_OUT inventory movement
     *
     * @param transferId The ID of the transfer to approve
     * @param approvedBy The user approving the transfer
     * @return The approved transfer with IN_TRANSIT status
     * @throws IllegalArgumentException if transfer not found
     * @throws IllegalStateException    if transfer is not PENDING or insufficient stock
     */
    BranchTransfer approveTransfer(Integer transferId, User approvedBy);

    /**
     * Receive a transfer at destination branch
     * Changes status from IN_TRANSIT to COMPLETED, increases stock at destination branch,
     * and creates TRANSFER_IN inventory movement
     *
     * @param transferId The ID of the transfer to receive
     * @param receivedBy The user receiving the transfer
     * @return The completed transfer with COMPLETED status
     * @throws IllegalArgumentException if transfer not found
     * @throws IllegalStateException    if transfer is not IN_TRANSIT
     */
    BranchTransfer receiveTransfer(Integer transferId, User receivedBy);

    /**
     * Cancel a transfer
     * Can cancel PENDING or IN_TRANSIT transfers.
     * If IN_TRANSIT, reverses the stock reduction at source branch
     *
     * @param transferId The ID of the transfer to cancel
     * @param reason     The cancellation reason (required)
     * @param cancelledBy The user cancelling the transfer
     * @return The cancelled transfer with CANCELLED status
     * @throws IllegalArgumentException if transfer not found or reason is empty
     * @throws IllegalStateException    if transfer is already COMPLETED
     */
    BranchTransfer cancelTransfer(Integer transferId, String reason, User cancelledBy);

    /**
     * Validate if sufficient stock is available in a batch
     *
     * @param batchId  The batch ID to check
     * @param quantity The quantity required
     * @return true if stock is sufficient, false otherwise
     */
    boolean validateStockAvailability(Integer batchId, Integer quantity);

    /**
     * Save a new transfer (typically not called directly, use requestTransfer instead)
     *
     * @param transfer The transfer to save
     * @return The saved transfer
     */
    BranchTransfer save(BranchTransfer transfer);

    /**
     * Update an existing transfer
     *
     * @param transfer The transfer to update
     * @return The updated transfer
     */
    BranchTransfer edit(BranchTransfer transfer);

    /**
     * Delete a transfer
     *
     * @param transfer The transfer to delete
     */
    void delete(BranchTransfer transfer);

    /**
     * List all transfers
     *
     * @return List of all transfers
     */
    List<BranchTransfer> list();

    /**
     * Find a transfer by ID
     *
     * @param id The transfer ID
     * @return The transfer if found, null otherwise
     */
    BranchTransfer findById(Integer id);
}
