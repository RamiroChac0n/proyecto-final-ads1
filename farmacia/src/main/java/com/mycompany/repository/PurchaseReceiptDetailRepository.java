package com.mycompany.repository;

import com.mycompany.model.entity.PurchaseReceiptDetail;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

/**
 * Repository for PurchaseReceiptDetail entity operations.
 * Provides data access methods for purchase receipt details.
 *
 * @author ramir
 */
@Stateless
public class PurchaseReceiptDetailRepository extends PharmacyRepository<PurchaseReceiptDetail> {

    @PersistenceContext
    private EntityManager em;

    public PurchaseReceiptDetailRepository() {
        super(PurchaseReceiptDetail.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Find all details for a specific purchase receipt.
     *
     * @param receiptId the purchase receipt ID
     * @return list of details for that receipt
     */
    public List<PurchaseReceiptDetail> findByReceiptId(Integer receiptId) {
        return em.createQuery(
                "SELECT prd FROM PurchaseReceiptDetail prd " +
                "WHERE prd.purchaseReceipt.receiptId = :receiptId " +
                "ORDER BY prd.createdAt",
                PurchaseReceiptDetail.class)
                .setParameter("receiptId", receiptId)
                .getResultList();
    }

    /**
     * Find all details for a specific product.
     *
     * @param productId the product ID
     * @return list of receipt details for that product
     */
    public List<PurchaseReceiptDetail> findByProductId(Integer productId) {
        return em.createQuery(
                "SELECT prd FROM PurchaseReceiptDetail prd " +
                "WHERE prd.product.productId = :productId " +
                "ORDER BY prd.createdAt DESC",
                PurchaseReceiptDetail.class)
                .setParameter("productId", productId)
                .getResultList();
    }

    /**
     * Find all details with a specific batch number.
     *
     * @param batchNumber the batch number
     * @return list of receipt details with that batch number
     */
    public List<PurchaseReceiptDetail> findByBatchNumber(String batchNumber) {
        return em.createQuery(
                "SELECT prd FROM PurchaseReceiptDetail prd " +
                "WHERE prd.batchNumber = :batchNumber " +
                "ORDER BY prd.createdAt DESC",
                PurchaseReceiptDetail.class)
                .setParameter("batchNumber", batchNumber)
                .getResultList();
    }

    /**
     * Find all details linked to a specific purchase order detail.
     *
     * @param orderDetailId the purchase order detail ID
     * @return list of receipt details linked to that order detail
     */
    public List<PurchaseReceiptDetail> findByOrderDetailId(Integer orderDetailId) {
        return em.createQuery(
                "SELECT prd FROM PurchaseReceiptDetail prd " +
                "WHERE prd.purchaseOrderDetail.detailId = :orderDetailId " +
                "ORDER BY prd.createdAt",
                PurchaseReceiptDetail.class)
                .setParameter("orderDetailId", orderDetailId)
                .getResultList();
    }

    /**
     * Find all details with damaged products.
     *
     * @return list of receipt details where quantityDamaged > 0
     */
    public List<PurchaseReceiptDetail> findDetailsWithDamage() {
        return em.createQuery(
                "SELECT prd FROM PurchaseReceiptDetail prd " +
                "WHERE prd.quantityDamaged > 0 " +
                "ORDER BY prd.createdAt DESC",
                PurchaseReceiptDetail.class)
                .getResultList();
    }

    /**
     * Calculate total quantity received for a product across all receipts.
     *
     * @param productId the product ID
     * @return total quantity received
     */
    public Long getTotalQuantityReceivedForProduct(Integer productId) {
        Long result = em.createQuery(
                "SELECT SUM(prd.quantityReceived) FROM PurchaseReceiptDetail prd " +
                "WHERE prd.product.productId = :productId",
                Long.class)
                .setParameter("productId", productId)
                .getSingleResult();
        return result != null ? result : 0L;
    }

    /**
     * Calculate total quantity damaged for a product across all receipts.
     *
     * @param productId the product ID
     * @return total quantity damaged
     */
    public Long getTotalQuantityDamagedForProduct(Integer productId) {
        Long result = em.createQuery(
                "SELECT SUM(prd.quantityDamaged) FROM PurchaseReceiptDetail prd " +
                "WHERE prd.product.productId = :productId",
                Long.class)
                .setParameter("productId", productId)
                .getSingleResult();
        return result != null ? result : 0L;
    }
}
