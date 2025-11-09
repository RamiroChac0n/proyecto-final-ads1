package com.mycompany.repository;

import com.mycompany.model.entity.Customer;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

/**
 * Repository for Customer entity
 * Provides data access methods for customer operations
 * @author ramir
 */
@Stateless
public class CustomerRepository extends PharmacyRepository<Customer> {

    @PersistenceContext
    private EntityManager em;

    public CustomerRepository() {
        super(Customer.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Find customer by tax ID (NIT)
     * @param taxId The tax ID to search for
     * @return Customer if found, null otherwise
     */
    public Customer findByTaxId(String taxId) {
        try {
            return em.createQuery("SELECT c FROM Customer c WHERE c.taxId = :taxId AND c.isActive = true", Customer.class)
                    .setParameter("taxId", taxId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Find customer by phone number
     * @param phone The phone number to search for
     * @return Customer if found, null otherwise
     */
    public Customer findByPhone(String phone) {
        try {
            return em.createQuery("SELECT c FROM Customer c WHERE c.phone = :phone AND c.isActive = true", Customer.class)
                    .setParameter("phone", phone)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Check if tax ID already exists
     * @param taxId Tax ID to check
     * @param excludeCustomerId Customer ID to exclude from check (for updates)
     * @return true if exists, false otherwise
     */
    public boolean taxIdExists(String taxId, Integer excludeCustomerId) {
        try {
            String query = "SELECT COUNT(c) FROM Customer c WHERE c.taxId = :taxId";
            if (excludeCustomerId != null) {
                query += " AND c.customerId != :customerId";
            }

            var q = em.createQuery(query, Long.class)
                    .setParameter("taxId", taxId);

            if (excludeCustomerId != null) {
                q.setParameter("customerId", excludeCustomerId);
            }

            return q.getSingleResult() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
