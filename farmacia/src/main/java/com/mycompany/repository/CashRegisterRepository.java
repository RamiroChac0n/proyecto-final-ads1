package com.mycompany.repository;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.CashRegister;
import com.mycompany.model.entity.enums.CashRegisterStatus;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for CashRegister entity operations
 * @author ramir
 */
@Stateless
public class CashRegisterRepository extends PharmacyRepository<CashRegister> {

    public CashRegisterRepository() {
        super(CashRegister.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Find the currently open register for a specific branch
     * @param branch The branch
     * @return CashRegister if found, null otherwise
     */
    public CashRegister findOpenByBranch(Branch branch) {
        try {
            TypedQuery<CashRegister> query = em.createQuery(
                "SELECT cr FROM CashRegister cr WHERE cr.branch = :branch AND cr.status = :status",
                CashRegister.class);
            query.setParameter("branch", branch);
            query.setParameter("status", CashRegisterStatus.OPEN);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Find all cash registers within a date range
     * @param fromDate Start date
     * @param toDate End date
     * @return List of CashRegisters
     */
    public List<CashRegister> findByDateRange(LocalDate fromDate, LocalDate toDate) {
        try {
            TypedQuery<CashRegister> query = em.createQuery(
                "SELECT cr FROM CashRegister cr " +
                "WHERE cr.openingDate >= :fromDate AND cr.openingDate <= :toDate " +
                "ORDER BY cr.openingDate DESC, cr.openingTime DESC",
                CashRegister.class);
            query.setParameter("fromDate", fromDate);
            query.setParameter("toDate", toDate);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find cash registers for a specific branch within a date range
     * @param branch The branch
     * @param fromDate Start date
     * @param toDate End date
     * @return List of CashRegisters
     */
    public List<CashRegister> findByBranchAndDateRange(Branch branch, LocalDate fromDate, LocalDate toDate) {
        try {
            TypedQuery<CashRegister> query = em.createQuery(
                "SELECT cr FROM CashRegister cr " +
                "WHERE cr.branch = :branch " +
                "AND cr.openingDate >= :fromDate AND cr.openingDate <= :toDate " +
                "ORDER BY cr.openingDate DESC, cr.openingTime DESC",
                CashRegister.class);
            query.setParameter("branch", branch);
            query.setParameter("fromDate", fromDate);
            query.setParameter("toDate", toDate);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Check if a branch has an open register
     * @param branch The branch
     * @return true if there is an open register, false otherwise
     */
    public boolean hasOpenRegister(Branch branch) {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(cr) FROM CashRegister cr " +
                "WHERE cr.branch = :branch AND cr.status = :status",
                Long.class);
            query.setParameter("branch", branch);
            query.setParameter("status", CashRegisterStatus.OPEN);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Find all open registers (for admin monitoring)
     * @return List of open CashRegisters
     */
    public List<CashRegister> findAllOpenRegisters() {
        try {
            TypedQuery<CashRegister> query = em.createQuery(
                "SELECT cr FROM CashRegister cr WHERE cr.status = :status " +
                "ORDER BY cr.openingDate DESC, cr.openingTime DESC",
                CashRegister.class);
            query.setParameter("status", CashRegisterStatus.OPEN);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }
}
