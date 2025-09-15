package com.mycompany.repository;

import com.mycompany.model.entity.DosageForm;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * Repository for DosageForm entities
 * @author ramir
 */
@Stateless
public class DosageFormRepository extends PharmacyRepository<DosageForm> {

    public DosageFormRepository() {
        super(DosageForm.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    public List<DosageForm> findAllOrderedByName() {
        TypedQuery<DosageForm> query = em.createQuery("SELECT df FROM DosageForm df ORDER BY df.formName", DosageForm.class);
        return query.getResultList();
    }
}