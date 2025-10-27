package com.mycompany.repository;

import com.mycompany.model.entity.PurchaseReceipt;
import com.mycompany.model.entity.enums.PurchaseReceiptStatus;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.Date;
import java.util.List;

/**
 * Repository for PurchaseReceipt entity operations.
 * Provides data access methods for purchase receipts.
 *
 * @author ramir
 */
@Stateless
public class PurchaseReceiptRepository extends PharmacyRepository<PurchaseReceipt> {

    @PersistenceContext
    private EntityManager em;

    public PurchaseReceiptRepository() {
        super(PurchaseReceipt.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Find a purchase receipt by receipt number.
     *
     * @param receiptNumber the unique receipt number
     * @return the purchase receipt if found, null otherwise
     */
    public PurchaseReceipt findByReceiptNumber(String receiptNumber) {
        try {
            return em.createQuery(
                    "SELECT pr FROM PurchaseReceipt pr WHERE pr.receiptNumber = :receiptNumber",
                    PurchaseReceipt.class)
                    .setParameter("receiptNumber", receiptNumber)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Find all receipts for a specific purchase order.
     *
     * @param orderId the purchase order ID
     * @return list of purchase receipts for that order
     */
    public List<PurchaseReceipt> findByOrderId(Integer orderId) {
        return em.createQuery(
                "SELECT pr FROM PurchaseReceipt pr " +
                "WHERE pr.purchaseOrder.orderId = :orderId " +
                "ORDER BY pr.receiptDate DESC",
                PurchaseReceipt.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    /**
     * Find all receipts with a specific status.
     *
     * @param status the status to filter by
     * @return list of purchase receipts with that status
     */
    public List<PurchaseReceipt> findByStatus(PurchaseReceiptStatus status) {
        return em.createQuery(
                "SELECT pr FROM PurchaseReceipt pr " +
                "WHERE pr.status = :status " +
                "ORDER BY pr.receiptDate DESC",
                PurchaseReceipt.class)
                .setParameter("status", status)
                .getResultList();
    }

    /**
     * Find all receipts created within a date range.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of purchase receipts in that date range
     */
    public List<PurchaseReceipt> findByDateRange(Date startDate, Date endDate) {
        return em.createQuery(
                "SELECT pr FROM PurchaseReceipt pr " +
                "WHERE pr.receiptDate BETWEEN :startDate AND :endDate " +
                "ORDER BY pr.receiptDate DESC",
                PurchaseReceipt.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    /**
     * Find a receipt by ID with details eagerly loaded.
     *
     * @param receiptId the receipt ID
     * @return the purchase receipt with details, or null if not found
     */
    public PurchaseReceipt findByIdWithDetails(Integer receiptId) {
        try {
            return em.createQuery(
                    "SELECT DISTINCT pr FROM PurchaseReceipt pr " +
                    "LEFT JOIN FETCH pr.receiptDetails " +
                    "LEFT JOIN FETCH pr.purchaseOrder " +
                    "WHERE pr.receiptId = :receiptId",
                    PurchaseReceipt.class)
                    .setParameter("receiptId", receiptId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Find all draft receipts (still being edited).
     *
     * @return list of draft purchase receipts
     */
    public List<PurchaseReceipt> findDraftReceipts() {
        return findByStatus(PurchaseReceiptStatus.DRAFT);
    }

    /**
     * Find all completed receipts.
     *
     * @return list of completed purchase receipts
     */
    public List<PurchaseReceipt> findCompletedReceipts() {
        return findByStatus(PurchaseReceiptStatus.COMPLETE);
    }

    /**
     * Count receipts by status.
     *
     * @param status the status to count
     * @return number of receipts with that status
     */
    public Long countByStatus(PurchaseReceiptStatus status) {
        return em.createQuery(
                "SELECT COUNT(pr) FROM PurchaseReceipt pr WHERE pr.status = :status",
                Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    /**
     * Find all receipts for a specific branch.
     *
     * @param branchId the branch ID
     * @return list of purchase receipts for that branch
     */
    public List<PurchaseReceipt> findByBranchId(Integer branchId) {
        return em.createQuery(
                "SELECT pr FROM PurchaseReceipt pr " +
                "WHERE pr.branch.branchId = :branchId " +
                "ORDER BY pr.receiptDate DESC",
                PurchaseReceipt.class)
                .setParameter("branchId", branchId)
                .getResultList();
    }

    /**
     * Find all receipts received by a specific user.
     *
     * @param userId the user ID who received products
     * @return list of purchase receipts received by that user
     */
    public List<PurchaseReceipt> findByReceivedBy(String userId) {
        return em.createQuery(
                "SELECT pr FROM PurchaseReceipt pr " +
                "WHERE pr.receivedBy = :userId " +
                "ORDER BY pr.receiptDate DESC",
                PurchaseReceipt.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
