package com.mycompany.repository;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.Product;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * Repository for Product entities
 * @author ramir
 */
@Stateless
public class ProductRepository extends PharmacyRepository<Product> {

    public ProductRepository() {
        super(Product.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    public List<Product> findActiveProducts() {
        TypedQuery<Product> query = em.createQuery("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.commercialName", Product.class);
        return query.getResultList();
    }
    
    public List<Product> findByCommercialName(String commercialName) {
        TypedQuery<Product> query = em.createQuery("SELECT p FROM Product p WHERE LOWER(p.commercialName) LIKE LOWER(:commercialName)", Product.class);
        query.setParameter("commercialName", "%" + commercialName + "%");
        return query.getResultList();
    }
    
    public List<Product> findByManufacturer(String manufacturer) {
        TypedQuery<Product> query = em.createQuery("SELECT p FROM Product p WHERE LOWER(p.manufacturer) LIKE LOWER(:manufacturer)", Product.class);
        query.setParameter("manufacturer", "%" + manufacturer + "%");
        return query.getResultList();
    }

    /**
     * Find active products that have available stock in a specific branch
     * @param branch The branch to filter by
     * @return List of products with available stock in the specified branch
     */
    public List<Product> findActiveProductsWithStockInBranch(Branch branch) {
        try {
            TypedQuery<Product> query = em.createQuery(
                "SELECT DISTINCT p FROM Product p " +
                "JOIN p.batches pb " +
                "WHERE p.isActive = true " +
                "AND pb.branch = :branch " +
                "AND pb.isActive = true " +
                "AND pb.isExpired = false " +
                "AND pb.quantityAvailable > 0 " +
                "ORDER BY p.commercialName ASC",
                Product.class);
            query.setParameter("branch", branch);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }
}