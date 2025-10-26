package com.mycompany.repository;

import com.mycompany.model.entity.Product;
import com.mycompany.model.entity.PurchaseOrder;
import com.mycompany.model.entity.PurchaseOrderDetail;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Repository for PurchaseOrderDetail entity.
 * Provides data access operations for purchase order details.
 *
 * @author ramir
 */
@Stateless
public class PurchaseOrderDetailRepository extends PharmacyRepository<PurchaseOrderDetail> {

    @PersistenceContext(unitName = "pharmacy-pu")
    private EntityManager em;

    public PurchaseOrderDetailRepository() {
        super(PurchaseOrderDetail.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Find all details for a specific purchase order.
     *
     * @param purchaseOrder the purchase order
     * @return list of order details
     */
    public List<PurchaseOrderDetail> findByPurchaseOrder(PurchaseOrder purchaseOrder) {
        TypedQuery<PurchaseOrderDetail> query = em.createQuery(
            "SELECT pod FROM PurchaseOrderDetail pod WHERE pod.purchaseOrder = :order",
            PurchaseOrderDetail.class
        );
        query.setParameter("order", purchaseOrder);
        return query.getResultList();
    }

    /**
     * Find all details for a specific product across all purchase orders.
     * Useful for tracking purchase history of a product.
     *
     * @param product the product
     * @return list of order details containing that product
     */
    public List<PurchaseOrderDetail> findByProduct(Product product) {
        TypedQuery<PurchaseOrderDetail> query = em.createQuery(
            "SELECT pod FROM PurchaseOrderDetail pod " +
            "WHERE pod.product = :product " +
            "ORDER BY pod.purchaseOrder.orderDate DESC",
            PurchaseOrderDetail.class
        );
        query.setParameter("product", product);
        return query.getResultList();
    }

    /**
     * Find details by purchase order ID.
     *
     * @param orderId the order ID
     * @return list of order details
     */
    public List<PurchaseOrderDetail> findByOrderId(Integer orderId) {
        TypedQuery<PurchaseOrderDetail> query = em.createQuery(
            "SELECT pod FROM PurchaseOrderDetail pod " +
            "WHERE pod.purchaseOrder.orderId = :orderId",
            PurchaseOrderDetail.class
        );
        query.setParameter("orderId", orderId);
        return query.getResultList();
    }

    /**
     * Delete all details for a specific purchase order.
     * Used when clearing an order's products.
     *
     * @param purchaseOrder the purchase order
     * @return number of details deleted
     */
    public int deleteByPurchaseOrder(PurchaseOrder purchaseOrder) {
        return em.createQuery(
            "DELETE FROM PurchaseOrderDetail pod WHERE pod.purchaseOrder = :order"
        )
        .setParameter("order", purchaseOrder)
        .executeUpdate();
    }
}
