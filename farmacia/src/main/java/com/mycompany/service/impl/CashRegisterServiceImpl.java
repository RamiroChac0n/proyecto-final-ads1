package com.mycompany.service.impl;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.CashRegister;
import com.mycompany.model.entity.User;
import com.mycompany.model.entity.enums.CashRegisterStatus;
import com.mycompany.model.entity.enums.Role;
import com.mycompany.repository.CashRegisterRepository;
import com.mycompany.service.ICashRegisterService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Implementation of CashRegister business operations
 * Only ADMIN users can open/close cash registers
 * @author ramir
 */
@Stateless
public class CashRegisterServiceImpl implements ICashRegisterService {

    @EJB
    private CashRegisterRepository cashRegisterRepository;

    @Override
    public CashRegister openCashRegister(Branch branch, User admin, BigDecimal initialCash) {
        // Validate ADMIN role
        validateAdminRole(admin);

        // Validate no open register exists for this branch
        if (hasOpenRegister(branch)) {
            throw new IllegalArgumentException("Ya existe una caja abierta para esta sucursal");
        }

        // Validate initial cash
        if (initialCash == null || initialCash.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto inicial debe ser mayor o igual a cero");
        }

        // Create new cash register
        CashRegister cashRegister = CashRegister.builder()
            .branch(branch)
            .openingDate(LocalDate.now())
            .openingTime(LocalTime.now())
            .openingUser(admin)
            .initialCash(initialCash)
            .totalSales(BigDecimal.ZERO)
            .totalTransactions(0)
            .status(CashRegisterStatus.OPEN)
            .build();

        return cashRegisterRepository.save(cashRegister);
    }

    @Override
    public CashRegister closeCashRegister(Integer registerId, User admin, BigDecimal finalCash, String notes) {
        // Validate ADMIN role
        validateAdminRole(admin);

        // Find cash register
        CashRegister cashRegister = findById(registerId);
        if (cashRegister == null) {
            throw new IllegalArgumentException("Caja no encontrada");
        }

        // Validate register is open
        if (!CashRegisterStatus.OPEN.equals(cashRegister.getStatus())) {
            throw new IllegalArgumentException("La caja ya está cerrada");
        }

        // Validate final cash
        if (finalCash == null || finalCash.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto final debe ser mayor o igual a cero");
        }

        // Calculate expected cash and difference
        BigDecimal expectedCash = calculateExpectedCash(cashRegister);
        BigDecimal cashDifference = finalCash.subtract(expectedCash);

        // Update cash register
        cashRegister.setStatus(CashRegisterStatus.CLOSED);
        cashRegister.setClosingTime(LocalTime.now());
        cashRegister.setClosingUser(admin);
        cashRegister.setFinalCash(finalCash);
        cashRegister.setCashDifference(cashDifference);
        cashRegister.setNotes(notes);

        return cashRegisterRepository.update(cashRegister);
    }

    @Override
    public CashRegister getOpenRegisterByBranch(Branch branch) {
        return cashRegisterRepository.findOpenByBranch(branch);
    }

    @Override
    public boolean hasOpenRegister(Branch branch) {
        return cashRegisterRepository.hasOpenRegister(branch);
    }

    @Override
    public List<CashRegister> getRegisterHistory(LocalDate fromDate, LocalDate toDate) {
        return cashRegisterRepository.findByDateRange(fromDate, toDate);
    }

    @Override
    public List<CashRegister> getRegisterHistoryByBranch(Branch branch, LocalDate fromDate, LocalDate toDate) {
        return cashRegisterRepository.findByBranchAndDateRange(branch, fromDate, toDate);
    }

    @Override
    public CashRegister findById(Integer registerId) {
        return cashRegisterRepository.findById(registerId);
    }

    @Override
    public BigDecimal calculateExpectedCash(CashRegister cashRegister) {
        if (cashRegister == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal initialCash = cashRegister.getInitialCash() != null
            ? cashRegister.getInitialCash() : BigDecimal.ZERO;
        BigDecimal totalSales = cashRegister.getTotalSales() != null
            ? cashRegister.getTotalSales() : BigDecimal.ZERO;
        return initialCash.add(totalSales);
    }

    @Override
    public void validateAdminRole(User user) {
        if (user == null || !Role.ADMIN.equals(user.getRole())) {
            throw new IllegalArgumentException("Solo usuarios ADMIN pueden realizar esta operación");
        }
    }
}
