package com.mycompany.repository;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.Sale;
import com.mycompany.model.entity.enums.SaleStatus;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Repository for Sale entity operations
 * @author ramir
 */
@Stateless
public class SaleRepository extends PharmacyRepository<Sale> {

    public SaleRepository() {
        super(Sale.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Find a sale by sale number
     * @param saleNumber The sale number
     * @return Sale if found, null otherwise
     */
    public Sale findBySaleNumber(String saleNumber) {
        try {
            TypedQuery<Sale> query = em.createQuery(
                "SELECT s FROM Sale s WHERE s.saleNumber = :saleNumber",
                Sale.class);
            query.setParameter("saleNumber", saleNumber);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Find sales by date range
     * @param fromDate Start date
     * @param toDate End date
     * @return List of sales in the date range
     */
    public List<Sale> findByDateRange(Date fromDate, Date toDate) {
        try {
            TypedQuery<Sale> query = em.createQuery(
                "SELECT s FROM Sale s WHERE s.saleDate BETWEEN :fromDate AND :toDate ORDER BY s.saleDate DESC",
                Sale.class);
            query.setParameter("fromDate", fromDate);
            query.setParameter("toDate", toDate);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find sales by status
     * @param status The sale status
     * @return List of sales with the given status
     */
    public List<Sale> findByStatus(SaleStatus status) {
        try {
            TypedQuery<Sale> query = em.createQuery(
                "SELECT s FROM Sale s WHERE s.saleStatus = :status ORDER BY s.saleDate DESC",
                Sale.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find all sales ordered by date descending
     * @return List of all sales
     */
    @Override
    public List<Sale> findAll() {
        try {
            TypedQuery<Sale> query = em.createQuery(
                "SELECT s FROM Sale s ORDER BY s.saleDate DESC",
                Sale.class);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find all sales with User loaded (JOIN FETCH)
     * Used for sales history view to avoid LazyInitializationException
     * @return List of all sales with user relationship loaded
     */
    public List<Sale> findAllWithUser() {
        try {
            TypedQuery<Sale> query = em.createQuery(
                "SELECT s FROM Sale s " +
                "LEFT JOIN FETCH s.user " +
                "ORDER BY s.saleDate DESC",
                Sale.class);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find sale by ID with all details loaded (JOIN FETCH)
     * Used for sale details dialog to avoid LazyInitializationException
     * @param saleId The sale ID
     * @return Sale with user, saleDetails, and products loaded, or null if not found
     */
    public Sale findByIdWithDetails(Integer saleId) {
        try {
            TypedQuery<Sale> query = em.createQuery(
                "SELECT DISTINCT s FROM Sale s " +
                "LEFT JOIN FETCH s.user " +
                "LEFT JOIN FETCH s.saleDetails sd " +
                "LEFT JOIN FETCH sd.product " +
                "WHERE s.saleId = :saleId",
                Sale.class);
            query.setParameter("saleId", saleId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Find sales by branch
     * @param branch The branch to filter by
     * @return List of sales for the given branch (excluding NULL branches)
     */
    public List<Sale> findByBranch(Branch branch) {
        try {
            TypedQuery<Sale> query = em.createQuery(
                "SELECT s FROM Sale s WHERE s.branch = :branch ORDER BY s.saleDate DESC",
                Sale.class);
            query.setParameter("branch", branch);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find sales by branch with User loaded (JOIN FETCH)
     * Used for sales history view to avoid LazyInitializationException
     * @param branch The branch to filter by
     * @return List of sales with user relationship loaded
     */
    public List<Sale> findByBranchWithUser(Branch branch) {
        try {
            TypedQuery<Sale> query = em.createQuery(
                "SELECT s FROM Sale s " +
                "LEFT JOIN FETCH s.user " +
                "WHERE s.branch = :branch " +
                "ORDER BY s.saleDate DESC",
                Sale.class);
            query.setParameter("branch", branch);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find sales by branch and date range
     * @param branch The branch to filter by
     * @param fromDate Start date
     * @param toDate End date
     * @return List of sales in the date range for the given branch
     */
    public List<Sale> findByBranchAndDateRange(Branch branch, Date fromDate, Date toDate) {
        try {
            TypedQuery<Sale> query = em.createQuery(
                "SELECT s FROM Sale s " +
                "WHERE s.branch = :branch " +
                "AND s.saleDate BETWEEN :fromDate AND :toDate " +
                "ORDER BY s.saleDate DESC",
                Sale.class);
            query.setParameter("branch", branch);
            query.setParameter("fromDate", fromDate);
            query.setParameter("toDate", toDate);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Find sales by branch and status
     * @param branch The branch to filter by
     * @param status The sale status
     * @return List of sales with the given status for the branch
     */
    public List<Sale> findByBranchAndStatus(Branch branch, SaleStatus status) {
        try {
            TypedQuery<Sale> query = em.createQuery(
                "SELECT s FROM Sale s " +
                "WHERE s.branch = :branch " +
                "AND s.saleStatus = :status " +
                "ORDER BY s.saleDate DESC",
                Sale.class);
            query.setParameter("branch", branch);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }
}
