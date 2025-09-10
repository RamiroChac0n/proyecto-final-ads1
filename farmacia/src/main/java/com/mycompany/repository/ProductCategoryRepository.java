package com.mycompany.repository;

import com.mycompany.model.entity.ProductCategory;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * Repository for ProductCategory entities
 * @author ramir
 */
@Stateless
public class ProductCategoryRepository extends PharmacyRepository<ProductCategory> {

    public ProductCategoryRepository() {
        super(ProductCategory.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    public List<ProductCategory> findActiveCategories() {
        TypedQuery<ProductCategory> query = em.createQuery("SELECT pc FROM ProductCategory pc WHERE pc.isActive = true ORDER BY pc.categoryName", ProductCategory.class);
        return query.getResultList();
    }
}