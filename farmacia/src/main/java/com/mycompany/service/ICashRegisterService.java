package com.mycompany.service;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.CashRegister;
import com.mycompany.model.entity.User;
import jakarta.ejb.Local;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Cash Register operations
 * Only ADMIN users can open/close cash registers
 * @author ramir
 */
@Local
public interface ICashRegisterService {

    /**
     * Open a new cash register
     * Only ADMIN can perform this operation
     * @param branch The branch where the register is opened
     * @param admin The ADMIN user opening the register
     * @param initialCash Initial cash amount
     * @return The opened CashRegister
     * @throws IllegalArgumentException if user is not ADMIN or branch already has open register
     */
    CashRegister openCashRegister(Branch branch, User admin, BigDecimal initialCash);

    /**
     * Close an open cash register
     * Only ADMIN can perform this operation
     * @param registerId The register ID to close
     * @param admin The ADMIN user closing the register
     * @param finalCash Final cash amount counted
     * @param notes Optional notes about the closing
     * @return The closed CashRegister
     * @throws IllegalArgumentException if user is not ADMIN or register not found/already closed
     */
    CashRegister closeCashRegister(Integer registerId, User admin, BigDecimal finalCash, String notes);

    /**
     * Get the currently open register for a branch
     * @param branch The branch
     * @return The open CashRegister or null if no register is open
     */
    CashRegister getOpenRegisterByBranch(Branch branch);

    /**
     * Check if a branch has an open register
     * @param branch The branch
     * @return true if there is an open register, false otherwise
     */
    boolean hasOpenRegister(Branch branch);

    /**
     * Get cash register history for a date range
     * @param fromDate Start date
     * @param toDate End date
     * @return List of CashRegisters in the date range
     */
    List<CashRegister> getRegisterHistory(LocalDate fromDate, LocalDate toDate);

    /**
     * Get cash register history for a specific branch and date range
     * @param branch The branch
     * @param fromDate Start date
     * @param toDate End date
     * @return List of CashRegisters
     */
    List<CashRegister> getRegisterHistoryByBranch(Branch branch, LocalDate fromDate, LocalDate toDate);

    /**
     * Find a cash register by ID
     * @param registerId The register ID
     * @return CashRegister if found, null otherwise
     */
    CashRegister findById(Integer registerId);

    /**
     * Calculate expected cash for a register
     * Expected = Initial Cash + Total Sales
     * @param cashRegister The cash register
     * @return Expected cash amount
     */
    BigDecimal calculateExpectedCash(CashRegister cashRegister);

    /**
     * Validate that user has ADMIN role
     * @param user The user to validate
     * @throws IllegalArgumentException if user is not ADMIN
     */
    void validateAdminRole(User user);

    /**
     * Update cash register totals after a sale
     * @param register The cash register to update
     * @param saleAmount The amount of the sale
     * @return Updated CashRegister
     */
    CashRegister updateRegisterAfterSale(CashRegister register, BigDecimal saleAmount);
}
