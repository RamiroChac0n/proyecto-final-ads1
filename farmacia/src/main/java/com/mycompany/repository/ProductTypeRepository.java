package com.mycompany.repository;

import com.mycompany.model.entity.ProductType;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * Repository for ProductType entities
 * @author ramir
 */
@Stateless
public class ProductTypeRepository extends PharmacyRepository<ProductType> {

    public ProductTypeRepository() {
        super(ProductType.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    public List<ProductType> findActiveTypes() {
        TypedQuery<ProductType> query = em.createQuery("SELECT pt FROM ProductType pt WHERE pt.isActive = true ORDER BY pt.typeName", ProductType.class);
        return query.getResultList();
    }
}