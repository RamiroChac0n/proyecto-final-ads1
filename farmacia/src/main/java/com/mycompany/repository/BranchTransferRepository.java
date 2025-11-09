package com.mycompany.repository;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.BranchTransfer;
import com.mycompany.model.entity.enums.TransferStatus;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Date;
import java.util.List;

/**
 * Repository for BranchTransfer entity operations
 * Provides data access methods for branch transfer management
 *
 * @author ramir
 */
@Stateless
public class BranchTransferRepository extends PharmacyRepository<BranchTransfer> {

    public BranchTransferRepository() {
        super(BranchTransfer.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Find all transfers by status
     *
     * @param status The transfer status to filter by
     * @return List of transfers with the specified status
     */
    public List<BranchTransfer> findByStatus(TransferStatus status) {
        try {
            TypedQuery<BranchTransfer> query = em.createQuery(
                    "SELECT t FROM BranchTransfer t WHERE t.status = :status ORDER BY t.transferDate DESC",
                    BranchTransfer.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find all transfers from a specific branch
     *
     * @param branch The source branch
     * @return List of transfers originating from the branch
     */
    public List<BranchTransfer> findByFromBranch(Branch branch) {
        try {
            TypedQuery<BranchTransfer> query = em.createQuery(
                    "SELECT t FROM BranchTransfer t WHERE t.fromBranch = :branch ORDER BY t.transferDate DESC",
                    BranchTransfer.class);
            query.setParameter("branch", branch);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find all transfers to a specific branch
     *
     * @param branch The destination branch
     * @return List of transfers destined for the branch
     */
    public List<BranchTransfer> findByToBranch(Branch branch) {
        try {
            TypedQuery<BranchTransfer> query = em.createQuery(
                    "SELECT t FROM BranchTransfer t WHERE t.toBranch = :branch ORDER BY t.transferDate DESC",
                    BranchTransfer.class);
            query.setParameter("branch", branch);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find transfers between a date range
     *
     * @param startDate Start date
     * @param endDate   End date
     * @return List of transfers within the date range
     */
    public List<BranchTransfer> findByDateRange(Date startDate, Date endDate) {
        try {
            TypedQuery<BranchTransfer> query = em.createQuery(
                    "SELECT t FROM BranchTransfer t WHERE t.transferDate BETWEEN :startDate AND :endDate ORDER BY t.transferDate DESC",
                    BranchTransfer.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find all transfers involving a specific branch (either as source or destination)
     *
     * @param branch The branch to search for
     * @return List of transfers involving the branch
     */
    public List<BranchTransfer> findByBranch(Branch branch) {
        try {
            TypedQuery<BranchTransfer> query = em.createQuery(
                    "SELECT t FROM BranchTransfer t WHERE t.fromBranch = :branch OR t.toBranch = :branch ORDER BY t.transferDate DESC",
                    BranchTransfer.class);
            query.setParameter("branch", branch);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find pending transfers for a specific branch (either as source or destination)
     * Useful for dashboards showing pending actions
     *
     * @param branch The branch
     * @return List of pending transfers for the branch
     */
    public List<BranchTransfer> findPendingByBranch(Branch branch) {
        try {
            TypedQuery<BranchTransfer> query = em.createQuery(
                    "SELECT t FROM BranchTransfer t WHERE (t.fromBranch = :branch OR t.toBranch = :branch) " +
                            "AND t.status = :status ORDER BY t.transferDate DESC",
                    BranchTransfer.class);
            query.setParameter("branch", branch);
            query.setParameter("status", TransferStatus.PENDING);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find in-transit transfers for a destination branch
     * Useful for showing transfers waiting to be received
     *
     * @param branch The destination branch
     * @return List of in-transit transfers destined for the branch
     */
    public List<BranchTransfer> findInTransitToBranch(Branch branch) {
        try {
            TypedQuery<BranchTransfer> query = em.createQuery(
                    "SELECT t FROM BranchTransfer t WHERE t.toBranch = :branch AND t.status = :status ORDER BY t.transferDate DESC",
                    BranchTransfer.class);
            query.setParameter("branch", branch);
            query.setParameter("status", TransferStatus.IN_TRANSIT);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }
}
