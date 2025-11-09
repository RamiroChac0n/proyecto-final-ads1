package com.mycompany.repository;

import com.mycompany.model.entity.Supplier;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

/**
 * Repository for Supplier entity operations.
 * Provides data access methods for supplier management.
 */
@Stateless
public class SupplierRepository extends PharmacyRepository<Supplier> {

    @PersistenceContext(unitName = "pharmacy-pu")
    private EntityManager em;

    public SupplierRepository() {
        super(Supplier.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Find a supplier by supplier code.
     *
     * @param supplierCode the supplier code to search for
     * @return the supplier if found, null otherwise
     */
    public Supplier findBySupplierCode(String supplierCode) {
        try {
            return em.createQuery(
                    "SELECT s FROM Supplier s WHERE s.supplierCode = :supplierCode",
                    Supplier.class)
                    .setParameter("supplierCode", supplierCode)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Find a supplier by tax ID (NIT).
     *
     * @param taxId the tax ID to search for
     * @return the supplier if found, null otherwise
     */
    public Supplier findByTaxId(String taxId) {
        try {
            return em.createQuery(
                    "SELECT s FROM Supplier s WHERE s.taxId = :taxId",
                    Supplier.class)
                    .setParameter("taxId", taxId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
