package com.mycompany.repository;

import com.mycompany.model.entity.ActivePrinciple;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * Repository for ActivePrinciple entities
 * @author ramir
 */
@Stateless
public class ActivePrincipleRepository extends PharmacyRepository<ActivePrinciple> {

    public ActivePrincipleRepository() {
        super(ActivePrinciple.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    public List<ActivePrinciple> findAllOrderedByName() {
        TypedQuery<ActivePrinciple> query = em.createQuery("SELECT ap FROM ActivePrinciple ap ORDER BY ap.innName", ActivePrinciple.class);
        return query.getResultList();
    }
}