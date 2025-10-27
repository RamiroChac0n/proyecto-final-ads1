package com.mycompany.service;

import com.mycompany.model.entity.PurchaseReceipt;
import com.mycompany.model.entity.PurchaseReceiptDetail;
import com.mycompany.model.entity.enums.PurchaseReceiptStatus;
import jakarta.ejb.Local;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Service interface for Purchase Receipt business logic.
 * Defines operations for receiving products, managing receipts, and creating inventory batches.
 *
 * @author ramir
 */
@Local
public interface IPurchaseReceiptService {

    /**
     * Start a new purchase receipt for an order.
     * Creates receipt in DRAFT status.
     *
     * @param orderId the purchase order ID
     * @param receivedBy the user ID of the storekeeper receiving products
     * @param branchId the branch ID where products are being received
     * @param supplierInvoiceNumber optional supplier invoice number
     * @param supplierInvoiceDate optional supplier invoice date
     * @return the new purchase receipt
     * @throws IllegalStateException if order is not in receivable status
     */
    PurchaseReceipt startReceipt(Integer orderId, String receivedBy, Integer branchId,
                                  String supplierInvoiceNumber, LocalDate supplierInvoiceDate);

    /**
     * Save a purchase receipt (create or update).
     * Can only save receipts in DRAFT status.
     *
     * @param receipt the receipt to save
     * @return the saved receipt
     * @throws IllegalStateException if receipt is not editable
     */
    PurchaseReceipt save(PurchaseReceipt receipt);

    /**
     * Update an existing purchase receipt.
     * Can only update receipts in DRAFT status.
     *
     * @param receipt the receipt to update
     * @return the updated receipt
     * @throws IllegalStateException if receipt is not editable
     */
    PurchaseReceipt edit(PurchaseReceipt receipt);

    /**
     * Delete a purchase receipt.
     * Can only delete receipts in DRAFT status.
     *
     * @param receipt the receipt to delete
     * @throws IllegalStateException if receipt cannot be deleted
     */
    void delete(PurchaseReceipt receipt);

    /**
     * Get all purchase receipts.
     *
     * @return list of all receipts
     */
    List<PurchaseReceipt> list();

    /**
     * Find a purchase receipt by ID.
     *
     * @param receiptId the receipt ID
     * @return the receipt if found, null otherwise
     */
    PurchaseReceipt findById(Integer receiptId);

    /**
     * Find a purchase receipt by ID with details eagerly loaded.
     *
     * @param receiptId the receipt ID
     * @return the receipt with details, or null if not found
     */
    PurchaseReceipt findByIdWithDetails(Integer receiptId);

    /**
     * Find receipts by purchase order.
     *
     * @param orderId the order ID
     * @return list of receipts for that order
     */
    List<PurchaseReceipt> findByOrderId(Integer orderId);

    /**
     * Find receipts by status.
     *
     * @param status the status to filter by
     * @return list of receipts with that status
     */
    List<PurchaseReceipt> findByStatus(PurchaseReceiptStatus status);

    /**
     * Find receipts within a date range.
     *
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return list of receipts in that range
     */
    List<PurchaseReceipt> findByDateRange(Date startDate, Date endDate);

    /**
     * Get all draft receipts (still being edited).
     *
     * @return list of draft receipts
     */
    List<PurchaseReceipt> findDraftReceipts();

    /**
     * Get all completed receipts.
     *
     * @return list of completed receipts
     */
    List<PurchaseReceipt> findCompletedReceipts();

    /**
     * Add a detail to a receipt.
     * Receipt must be in DRAFT status.
     *
     * @param receipt the receipt
     * @param detail the detail to add
     * @return the updated receipt
     * @throws IllegalStateException if receipt is not editable
     */
    PurchaseReceipt addDetail(PurchaseReceipt receipt, PurchaseReceiptDetail detail);

    /**
     * Remove a detail from a receipt.
     * Receipt must be in DRAFT status.
     *
     * @param receipt the receipt
     * @param detail the detail to remove
     * @return the updated receipt
     * @throws IllegalStateException if receipt is not editable
     */
    PurchaseReceipt removeDetail(PurchaseReceipt receipt, PurchaseReceiptDetail detail);

    /**
     * Complete a purchase receipt.
     * Changes status to COMPLETE and triggers:
     * - Creation of product batches
     * - Inventory movements
     * - Purchase order status update
     *
     * This is the critical method that processes the receipt.
     *
     * @param receiptId the receipt ID
     * @param completedBy the user ID completing the receipt
     * @param isFinal true if this is the final receipt for the order
     * @param notes optional completion notes
     * @return the completed receipt
     * @throws IllegalStateException if receipt cannot be completed
     * @throws IllegalArgumentException if receipt has no details
     */
    PurchaseReceipt completeReceipt(Integer receiptId, String completedBy,
                                     Boolean isFinal, String notes);

    /**
     * Validate that a receipt can be completed.
     * Checks:
     * - Receipt is in DRAFT status
     * - Receipt has at least one detail
     * - All details have valid data
     * - Damaged quantities <= received quantities
     *
     * @param receipt the receipt to validate
     * @return true if valid, false otherwise
     */
    boolean validateReceipt(PurchaseReceipt receipt);

    /**
     * Check if a receipt can be edited.
     *
     * @param receipt the receipt
     * @return true if receipt can be edited, false otherwise
     */
    boolean canEdit(PurchaseReceipt receipt);

    /**
     * Check if a receipt can be deleted.
     *
     * @param receipt the receipt
     * @return true if receipt can be deleted, false otherwise
     */
    boolean canDelete(PurchaseReceipt receipt);

    /**
     * Check if a receipt can be completed.
     *
     * @param receipt the receipt
     * @return true if receipt can be completed, false otherwise
     */
    boolean canComplete(PurchaseReceipt receipt);

    /**
     * Check if a purchase order can have a receipt started.
     * Order must be in CONFIRMED, IN_TRANSIT, or RECEIVING status.
     *
     * @param orderId the order ID
     * @return true if order can be received, false otherwise
     */
    boolean canStartReceiptForOrder(Integer orderId);

    /**
     * Generate a unique receipt number.
     * Format: REC-{year}-{sequence} or REC-{year}-{sequence}-P{partial}
     *
     * @param orderId the order ID (used to determine if partial)
     * @return generated receipt number
     */
    String generateReceiptNumber(Integer orderId);

    /**
     * Count receipts by status.
     *
     * @param status the status
     * @return count of receipts with that status
     */
    Long countByStatus(PurchaseReceiptStatus status);
}
