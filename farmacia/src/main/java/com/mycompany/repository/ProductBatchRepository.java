package com.mycompany.repository;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.Product;
import com.mycompany.model.entity.ProductBatch;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.Date;
import java.util.List;

/**
 * Repository for ProductBatch entity operations
 * @author ramir
 */
@Stateless
public class ProductBatchRepository extends PharmacyRepository<ProductBatch> {

    public ProductBatchRepository() {
        super(ProductBatch.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Find all batches for a specific product
     * @param product The product to search batches for
     * @return List of product batches
     */
    public List<ProductBatch> findByProduct(Product product) {
        try {
            TypedQuery<ProductBatch> query = em.createQuery(
                "SELECT pb FROM ProductBatch pb WHERE pb.product = :product ORDER BY pb.expirationDate ASC",
                ProductBatch.class);
            query.setParameter("product", product);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find a specific batch by product and batch number
     * @param product The product
     * @param batchNumber The batch number
     * @return ProductBatch if found, null otherwise
     */
    public ProductBatch findByProductAndBatchNumber(Product product, String batchNumber) {
        try {
            TypedQuery<ProductBatch> query = em.createQuery(
                "SELECT pb FROM ProductBatch pb WHERE pb.product = :product AND pb.batchNumber = :batchNumber",
                ProductBatch.class);
            query.setParameter("product", product);
            query.setParameter("batchNumber", batchNumber);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Find all available batches (quantity > 0)
     * @return List of available product batches
     */
    public List<ProductBatch> findAvailableBatches() {
        try {
            TypedQuery<ProductBatch> query = em.createQuery(
                "SELECT pb FROM ProductBatch pb WHERE pb.quantityAvailable > 0 AND pb.isActive = true ORDER BY pb.expirationDate ASC",
                ProductBatch.class);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find batches that expire before the given date
     * @param date The expiration date threshold
     * @return List of batches expiring before the date
     */
    public List<ProductBatch> findByExpirationDateBefore(Date date) {
        try {
            TypedQuery<ProductBatch> query = em.createQuery(
                "SELECT pb FROM ProductBatch pb WHERE pb.expirationDate < :date AND pb.isActive = true ORDER BY pb.expirationDate ASC",
                ProductBatch.class);
            query.setParameter("date", date);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find active batches for a specific product
     * @param product The product
     * @return List of active product batches
     */
    public List<ProductBatch> findActiveByProduct(Product product) {
        try {
            TypedQuery<ProductBatch> query = em.createQuery(
                "SELECT pb FROM ProductBatch pb WHERE pb.product = :product AND pb.isActive = true ORDER BY pb.expirationDate ASC",
                ProductBatch.class);
            query.setParameter("product", product);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find all batches for a specific product filtered by branch
     * @param product The product to search batches for
     * @param branch The branch to filter by
     * @return List of product batches in the specified branch
     */
    public List<ProductBatch> findByProductAndBranch(Product product, Branch branch) {
        try {
            TypedQuery<ProductBatch> query = em.createQuery(
                "SELECT pb FROM ProductBatch pb WHERE pb.product = :product AND pb.branch = :branch ORDER BY pb.expirationDate ASC",
                ProductBatch.class);
            query.setParameter("product", product);
            query.setParameter("branch", branch);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Check if a batch number already exists for a product
     * @param product The product
     * @param batchNumber The batch number to check
     * @return true if batch number exists, false otherwise
     */
    public boolean existsByProductAndBatchNumber(Product product, String batchNumber) {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(pb) FROM ProductBatch pb WHERE pb.product = :product AND pb.batchNumber = :batchNumber",
                Long.class);
            query.setParameter("product", product);
            query.setParameter("batchNumber", batchNumber);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Find available batches for a specific product using FIFO ordering
     * FIFO: First In, First Out / PEPS: Primero en Entrar, Primero en Salir
     * Orders by expiration date (earliest first), then received date, then batch ID
     * Only includes active, non-expired batches with available quantity
     *
     * @param product The product to search batches for
     * @return List of available product batches ordered by FIFO
     */
    public List<ProductBatch> findAvailableBatchesByProductFIFO(Product product) {
        try {
            TypedQuery<ProductBatch> query = em.createQuery(
                "SELECT pb FROM ProductBatch pb " +
                "WHERE pb.product = :product " +
                "AND pb.isActive = true " +
                "AND pb.isExpired = false " +
                "AND pb.quantityAvailable > 0 " +
                "ORDER BY pb.expirationDate ASC, pb.receivedDate ASC, pb.batchId ASC",
                ProductBatch.class);
            query.setParameter("product", product);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }
}