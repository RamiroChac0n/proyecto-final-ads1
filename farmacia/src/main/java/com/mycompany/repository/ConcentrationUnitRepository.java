package com.mycompany.repository;

import com.mycompany.model.entity.ConcentrationUnit;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * Repository for ConcentrationUnit entities
 * @author ramir
 */
@Stateless
public class ConcentrationUnitRepository extends PharmacyRepository<ConcentrationUnit> {

    public ConcentrationUnitRepository() {
        super(ConcentrationUnit.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    public List<ConcentrationUnit> findAllOrderedByName() {
        TypedQuery<ConcentrationUnit> query = em.createQuery("SELECT cu FROM ConcentrationUnit cu ORDER BY cu.unitName", ConcentrationUnit.class);
        return query.getResultList();
    }
}