package com.mycompany.service;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.PurchaseOrder;
import com.mycompany.model.entity.PurchaseOrderDetail;
import com.mycompany.model.entity.Supplier;
import com.mycompany.model.entity.enums.PurchaseOrderStatus;
import jakarta.ejb.Local;

import java.util.Date;
import java.util.List;

/**
 * Service interface for Purchase Order business logic.
 * Defines operations for creating, managing, and tracking purchase orders.
 *
 * @author ramir
 */
@Local
public interface IPurchaseOrderService {

    /**
     * Save a new purchase order.
     * Generates order number and sets initial status to DRAFT.
     *
     * @param order the purchase order to save
     * @return the saved purchase order with generated ID and order number
     */
    PurchaseOrder save(PurchaseOrder order);

    /**
     * Update an existing purchase order.
     * Recalculates totals and updates timestamp.
     *
     * @param order the purchase order to update
     * @return the updated purchase order
     * @throws IllegalStateException if order is not editable (not in DRAFT status)
     */
    PurchaseOrder edit(PurchaseOrder order);

    /**
     * Delete a purchase order.
     * Only DRAFT orders can be deleted.
     *
     * @param order the purchase order to delete
     * @throws IllegalStateException if order cannot be deleted
     */
    void delete(PurchaseOrder order);

    /**
     * Get all purchase orders.
     *
     * @return list of all purchase orders
     */
    List<PurchaseOrder> list();

    /**
     * Find a purchase order by ID.
     *
     * @param orderId the order ID
     * @return the purchase order if found, null otherwise
     */
    PurchaseOrder findById(Integer orderId);

    /**
     * Find a purchase order by ID with details eagerly loaded.
     *
     * @param orderId the order ID
     * @return the purchase order with details, or null if not found
     */
    PurchaseOrder findByIdWithDetails(Integer orderId);

    /**
     * Find a purchase order by order number.
     *
     * @param orderNumber the unique order number
     * @return the purchase order if found, null otherwise
     */
    PurchaseOrder findByOrderNumber(String orderNumber);

    /**
     * Get all purchase orders for a specific supplier.
     *
     * @param supplier the supplier
     * @return list of purchase orders for that supplier
     */
    List<PurchaseOrder> findBySupplier(Supplier supplier);

    /**
     * Get all purchase orders for a specific branch.
     *
     * @param branch the branch
     * @return list of purchase orders for that branch
     */
    List<PurchaseOrder> findByBranch(Branch branch);

    /**
     * Get all purchase orders with a specific status.
     *
     * @param status the status to filter by
     * @return list of purchase orders with that status
     */
    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);

    /**
     * Get all purchase orders created within a date range.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of purchase orders in that date range
     */
    List<PurchaseOrder> findByDateRange(Date startDate, Date endDate);

    /**
     * Get all pending orders (DRAFT or SENT status).
     *
     * @return list of pending purchase orders
     */
    List<PurchaseOrder> findPendingOrders();

    /**
     * Add a product detail to a purchase order.
     * Order must be in DRAFT status.
     *
     * @param order the purchase order
     * @param detail the detail to add
     * @return the updated purchase order
     * @throws IllegalStateException if order is not editable
     */
    PurchaseOrder addDetail(PurchaseOrder order, PurchaseOrderDetail detail);

    /**
     * Remove a product detail from a purchase order.
     * Order must be in DRAFT status.
     *
     * @param order the purchase order
     * @param detail the detail to remove
     * @return the updated purchase order
     * @throws IllegalStateException if order is not editable
     */
    PurchaseOrder removeDetail(PurchaseOrder order, PurchaseOrderDetail detail);

    /**
     * Send the order to the supplier.
     * Changes status from DRAFT to SENT.
     * Sets sentDate to current timestamp.
     *
     * @param orderId the order ID
     * @return the updated purchase order
     * @throws IllegalStateException if order is not in DRAFT status
     * @throws IllegalArgumentException if order has no details
     */
    PurchaseOrder sendOrder(Integer orderId);

    /**
     * Confirm the order (supplier accepted).
     * Changes status from SENT to CONFIRMED.
     * Sets confirmedDate to current timestamp.
     *
     * @param orderId the order ID
     * @param supplierConfirmationNumber optional confirmation number from supplier
     * @return the updated purchase order
     * @throws IllegalStateException if order is not in SENT status
     */
    PurchaseOrder confirmOrder(Integer orderId, String supplierConfirmationNumber);

    /**
     * Cancel a purchase order.
     * Only DRAFT or SENT orders can be cancelled.
     * Sets status to CANCELLED, records cancellation reason and user.
     *
     * @param orderId the order ID
     * @param userId the user ID who is cancelling
     * @param reason the reason for cancellation
     * @return the cancelled purchase order
     * @throws IllegalStateException if order cannot be cancelled
     */
    PurchaseOrder cancelOrder(Integer orderId, String userId, String reason);

    /**
     * Generate a unique order number for a new purchase order.
     * Format: OC-{branchId}-{year}-{sequence}
     * Example: OC-1-2025-000001
     *
     * @param branch the branch creating the order
     * @return the generated order number
     */
    String generateOrderNumber(Branch branch);

    /**
     * Validate that an order can be edited.
     *
     * @param order the purchase order
     * @return true if order can be edited, false otherwise
     */
    boolean canEdit(PurchaseOrder order);

    /**
     * Validate that an order can be deleted.
     *
     * @param order the purchase order
     * @return true if order can be deleted, false otherwise
     */
    boolean canDelete(PurchaseOrder order);

    /**
     * Validate that an order can be cancelled.
     *
     * @param order the purchase order
     * @return true if order can be cancelled, false otherwise
     */
    boolean canCancel(PurchaseOrder order);

    /**
     * Count orders by status.
     *
     * @param status the status to count
     * @return number of orders with that status
     */
    Long countByStatus(PurchaseOrderStatus status);
}
