package com.mycompany.repository;

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
}
