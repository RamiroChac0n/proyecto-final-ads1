package com.mycompany.repository;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.PurchaseOrder;
import com.mycompany.model.entity.Supplier;
import com.mycompany.model.entity.enums.PurchaseOrderStatus;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.Date;
import java.util.List;

/**
 * Repository for PurchaseOrder entity.
 * Provides data access operations for purchase orders.
 *
 * @author ramir
 */
@Stateless
public class PurchaseOrderRepository extends PharmacyRepository<PurchaseOrder> {

    @PersistenceContext(unitName = "pharmacy-pu")
    private EntityManager em;

    public PurchaseOrderRepository() {
        super(PurchaseOrder.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Find a purchase order by its order number.
     *
     * @param orderNumber the unique order number
     * @return the purchase order if found, null otherwise
     */
    public PurchaseOrder findByOrderNumber(String orderNumber) {
        try {
            TypedQuery<PurchaseOrder> query = em.createQuery(
                "SELECT po FROM PurchaseOrder po WHERE po.orderNumber = :orderNumber",
                PurchaseOrder.class
            );
            query.setParameter("orderNumber", orderNumber);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Find all purchase orders for a specific supplier.
     *
     * @param supplier the supplier
     * @return list of purchase orders for that supplier
     */
    public List<PurchaseOrder> findBySupplier(Supplier supplier) {
        TypedQuery<PurchaseOrder> query = em.createQuery(
            "SELECT po FROM PurchaseOrder po WHERE po.supplier = :supplier ORDER BY po.orderDate DESC",
            PurchaseOrder.class
        );
        query.setParameter("supplier", supplier);
        return query.getResultList();
    }

    /**
     * Find all purchase orders for a specific branch.
     *
     * @param branch the branch
     * @return list of purchase orders for that branch
     */
    public List<PurchaseOrder> findByBranch(Branch branch) {
        TypedQuery<PurchaseOrder> query = em.createQuery(
            "SELECT po FROM PurchaseOrder po WHERE po.branch = :branch ORDER BY po.orderDate DESC",
            PurchaseOrder.class
        );
        query.setParameter("branch", branch);
        return query.getResultList();
    }

    /**
     * Find all purchase orders with a specific status.
     *
     * @param status the purchase order status
     * @return list of purchase orders with that status
     */
    public List<PurchaseOrder> findByStatus(PurchaseOrderStatus status) {
        TypedQuery<PurchaseOrder> query = em.createQuery(
            "SELECT po FROM PurchaseOrder po WHERE po.status = :status ORDER BY po.orderDate DESC",
            PurchaseOrder.class
        );
        query.setParameter("status", status);
        return query.getResultList();
    }

    /**
     * Find all purchase orders created by a specific user.
     *
     * @param userId the user ID who created the orders
     * @return list of purchase orders created by that user
     */
    public List<PurchaseOrder> findByCreatedBy(String userId) {
        TypedQuery<PurchaseOrder> query = em.createQuery(
            "SELECT po FROM PurchaseOrder po WHERE po.createdBy = :userId ORDER BY po.orderDate DESC",
            PurchaseOrder.class
        );
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    /**
     * Find all purchase orders within a date range.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of purchase orders in that date range
     */
    public List<PurchaseOrder> findByDateRange(Date startDate, Date endDate) {
        TypedQuery<PurchaseOrder> query = em.createQuery(
            "SELECT po FROM PurchaseOrder po " +
            "WHERE po.orderDate >= :startDate AND po.orderDate <= :endDate " +
            "ORDER BY po.orderDate DESC",
            PurchaseOrder.class
        );
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }

    /**
     * Find all purchase orders with eagerly fetched details.
     * Useful for display/report generation to avoid N+1 queries.
     *
     * @return list of purchase orders with details loaded
     */
    public List<PurchaseOrder> findAllWithDetails() {
        TypedQuery<PurchaseOrder> query = em.createQuery(
            "SELECT DISTINCT po FROM PurchaseOrder po " +
            "LEFT JOIN FETCH po.orderDetails " +
            "ORDER BY po.orderDate DESC",
            PurchaseOrder.class
        );
        return query.getResultList();
    }

    /**
     * Find a purchase order by ID with eagerly fetched details.
     *
     * @param orderId the order ID
     * @return the purchase order with details loaded, or null if not found
     */
    public PurchaseOrder findByIdWithDetails(Integer orderId) {
        try {
            TypedQuery<PurchaseOrder> query = em.createQuery(
                "SELECT po FROM PurchaseOrder po " +
                "LEFT JOIN FETCH po.orderDetails " +
                "WHERE po.orderId = :orderId",
                PurchaseOrder.class
            );
            query.setParameter("orderId", orderId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Count purchase orders by status.
     *
     * @param status the status to count
     * @return number of orders with that status
     */
    public Long countByStatus(PurchaseOrderStatus status) {
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(po) FROM PurchaseOrder po WHERE po.status = :status",
            Long.class
        );
        query.setParameter("status", status);
        return query.getSingleResult();
    }

    /**
     * Find all pending orders (DRAFT or SENT status).
     * These are orders that need attention.
     *
     * @return list of pending purchase orders
     */
    public List<PurchaseOrder> findPendingOrders() {
        TypedQuery<PurchaseOrder> query = em.createQuery(
            "SELECT po FROM PurchaseOrder po " +
            "WHERE po.status IN (:draft, :sent) " +
            "ORDER BY po.orderDate ASC",
            PurchaseOrder.class
        );
        query.setParameter("draft", PurchaseOrderStatus.DRAFT);
        query.setParameter("sent", PurchaseOrderStatus.SENT);
        return query.getResultList();
    }

    /**
     * Override findAll to order by date descending.
     *
     * @return all purchase orders ordered by date
     */
    @Override
    public List<PurchaseOrder> findAll() {
        TypedQuery<PurchaseOrder> query = em.createQuery(
            "SELECT po FROM PurchaseOrder po ORDER BY po.orderDate DESC",
            PurchaseOrder.class
        );
        return query.getResultList();
    }
}
