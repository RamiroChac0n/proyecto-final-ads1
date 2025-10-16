package com.mycompany.repository;

import com.mycompany.model.entity.Branch;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Repository for Branch entity operations
 * @author ramir
 */
@Stateless
public class BranchRepository extends PharmacyRepository<Branch> {

    public BranchRepository() {
        super(Branch.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Find all active branches
     * @return List of active branches
     */
    public List<Branch> findActiveBranches() {
        try {
            TypedQuery<Branch> query = em.createQuery(
                "SELECT b FROM Branch b WHERE b.isActive = true ORDER BY b.branchName",
                Branch.class);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find branch by name
     * @param branchName The branch name
     * @return Branch if found, null otherwise
     */
    public Branch findByName(String branchName) {
        try {
            TypedQuery<Branch> query = em.createQuery(
                "SELECT b FROM Branch b WHERE b.branchName = :branchName",
                Branch.class);
            query.setParameter("branchName", branchName);
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
