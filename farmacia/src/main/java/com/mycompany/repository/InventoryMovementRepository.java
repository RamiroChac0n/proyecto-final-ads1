package com.mycompany.repository;

import com.mycompany.model.entity.InventoryMovement;
import com.mycompany.model.entity.Product;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * Repository for InventoryMovement entities
 * @author ramir
 */
@Stateless
public class InventoryMovementRepository extends PharmacyRepository<InventoryMovement> {

    public InventoryMovementRepository() {
        super(InventoryMovement.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Find all inventory movements for a specific product
     * Ordered by movement date descending (most recent first)
     * Uses JOIN FETCH to avoid LazyInitializationException
     *
     * @param product The product to find movements for
     * @return List of inventory movements
     */
    public List<InventoryMovement> findByProduct(Product product) {
        TypedQuery<InventoryMovement> query = em.createQuery(
            "SELECT im FROM InventoryMovement im " +
            "LEFT JOIN FETCH im.batch " +
            "LEFT JOIN FETCH im.branch " +
            "LEFT JOIN FETCH im.user " +
            "WHERE im.product = :product " +
            "ORDER BY im.movementDate DESC",
            InventoryMovement.class
        );
        query.setParameter("product", product);
        return query.getResultList();
    }

    /**
     * Find all inventory movements for a specific product by product ID
     *
     * @param productId The product ID to find movements for
     * @return List of inventory movements
     */
    public List<InventoryMovement> findByProductId(Long productId) {
        TypedQuery<InventoryMovement> query = em.createQuery(
            "SELECT im FROM InventoryMovement im " +
            "LEFT JOIN FETCH im.batch " +
            "LEFT JOIN FETCH im.branch " +
            "LEFT JOIN FETCH im.user " +
            "WHERE im.product.productId = :productId " +
            "ORDER BY im.movementDate DESC",
            InventoryMovement.class
        );
        query.setParameter("productId", productId);
        return query.getResultList();
    }
}
