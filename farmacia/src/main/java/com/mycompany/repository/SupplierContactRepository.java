package com.mycompany.repository;

import com.mycompany.model.entity.Supplier;
import com.mycompany.model.entity.SupplierContact;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 * Repository for SupplierContact entity operations.
 * Provides data access methods for supplier contact management.
 */
@Stateless
public class SupplierContactRepository extends PharmacyRepository<SupplierContact> {

    @PersistenceContext(unitName = "pharmacy-pu")
    private EntityManager em;

    public SupplierContactRepository() {
        super(SupplierContact.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Find all contacts for a specific supplier.
     *
     * @param supplier the supplier to find contacts for
     * @return list of contacts for the supplier
     */
    public List<SupplierContact> findBySupplier(Supplier supplier) {
        return em.createQuery(
                "SELECT sc FROM SupplierContact sc WHERE sc.supplier = :supplier ORDER BY sc.isPrimary DESC, sc.contactName ASC",
                SupplierContact.class)
                .setParameter("supplier", supplier)
                .getResultList();
    }
}
