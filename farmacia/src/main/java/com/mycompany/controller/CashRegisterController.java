package com.mycompany.controller;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.CashRegister;
import com.mycompany.model.entity.User;
import com.mycompany.model.entity.enums.CashRegisterStatus;
import com.mycompany.repository.BranchRepository;
import com.mycompany.service.ICashRegisterService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Data;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for Cash Register operations (ADMIN only)
 * Only ADMIN users can open and close cash registers
 * @author ramir
 */
@Data
@Named(value = "cashRegisterController")
@ViewScoped
public class CashRegisterController implements Serializable {

    @EJB
    private ICashRegisterService cashRegisterService;

    @EJB
    private BranchRepository branchRepository;

    @Inject
    private UserController userController;

    // Cash register fields
    private CashRegister selectedRegister;
    private List<CashRegister> cashRegisters;
    private List<CashRegister> openRegisters;

    // For opening a new register
    private Integer selectedBranchId;
    private BigDecimal initialCash;

    // For closing a register
    private BigDecimal finalCash;
    private String closingNotes;

    // Search filters
    private LocalDate fromDate;
    private LocalDate toDate;

    // Available branches
    private List<Branch> branches;

    @PostConstruct
    public void init() {
        loadBranches();
        loadOpenRegisters();
        initializeDateRange();
        searchRegisters();
    }

    /**
     * Load all active branches
     */
    public void loadBranches() {
        branches = branchRepository.findAll().stream()
            .filter(Branch::getIsActive)
            .toList();
    }

    /**
     * Load all currently open registers
     */
    public void loadOpenRegisters() {
        openRegisters = cashRegisterService.getRegisterHistory(
            LocalDate.now().minusDays(7),
            LocalDate.now()
        ).stream()
        .filter(cr -> CashRegisterStatus.OPEN.equals(cr.getStatus()))
        .toList();
    }

    /**
     * Initialize date range to last 30 days
     */
    public void initializeDateRange() {
        toDate = LocalDate.now();
        fromDate = toDate.minusDays(30);
    }

    /**
     * Search cash registers by date range
     */
    public void searchRegisters() {
        if (fromDate == null) {
            fromDate = LocalDate.now().minusDays(30);
        }
        if (toDate == null) {
            toDate = LocalDate.now();
        }

        cashRegisters = cashRegisterService.getRegisterHistory(fromDate, toDate);
    }

    /**
     * Prepare to open a new cash register
     */
    public void prepareOpenRegister() {
        User currentUser = userController.getCurrentUser();

        // Validate ADMIN role
        if (!userController.isAdmin()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Acceso denegado",
                    "Solo administradores pueden abrir cajas"));
            return;
        }

        // TODO: Implementar relación User-Branch
        // Default to current user's branch
        /*
        if (currentUser != null && currentUser.getBranch() != null) {
            selectedBranchId = currentUser.getBranch().getBranchId();
        }
        */

        initialCash = BigDecimal.ZERO;
    }

    /**
     * Open a new cash register
     */
    public void openRegister() {
        try {
            User admin = userController.getCurrentUser();

            // Validate admin role (service will also validate)
            if (!userController.isAdmin()) {
                throw new IllegalArgumentException("Solo administradores pueden abrir cajas");
            }

            // Validate branch selected
            if (selectedBranchId == null) {
                throw new IllegalArgumentException("Debe seleccionar una sucursal");
            }

            // Validate initial cash
            if (initialCash == null || initialCash.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El monto inicial debe ser mayor o igual a cero");
            }

            // Find branch
            Branch branch = branchRepository.findById(selectedBranchId);
            if (branch == null) {
                throw new IllegalArgumentException("Sucursal no encontrada");
            }

            // Open register through service
            CashRegister openedRegister = cashRegisterService.openCashRegister(branch, admin, initialCash);

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Caja abierta",
                    "Caja abierta exitosamente para " + branch.getBranchName()));

            // Refresh lists
            loadOpenRegisters();
            searchRegisters();

            PrimeFaces.current().executeScript("PF('dlgOpenRegister').hide()");
            PrimeFaces.current().ajax().update("form:messages", "form:open-registers", "form:dt-registers");

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al abrir caja",
                    e.getMessage()));
        }
    }

    /**
     * Prepare to close a cash register
     */
    public void prepareCloseRegister(CashRegister register) {
        // Validate ADMIN role
        if (!userController.isAdmin()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Acceso denegado",
                    "Solo administradores pueden cerrar cajas"));
            return;
        }

        selectedRegister = register;
        finalCash = BigDecimal.ZERO;
        closingNotes = "";

        // Calculate expected cash for display
        BigDecimal expected = cashRegisterService.calculateExpectedCash(register);
    }

    /**
     * Close the selected cash register
     */
    public void closeRegister() {
        try {
            User admin = userController.getCurrentUser();

            // Validate admin role
            if (!userController.isAdmin()) {
                throw new IllegalArgumentException("Solo administradores pueden cerrar cajas");
            }

            if (selectedRegister == null) {
                throw new IllegalArgumentException("No se ha seleccionado una caja");
            }

            // Validate final cash
            if (finalCash == null || finalCash.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El monto final debe ser mayor o igual a cero");
            }

            // Close register through service
            CashRegister closedRegister = cashRegisterService.closeCashRegister(
                selectedRegister.getRegisterId(),
                admin,
                finalCash,
                closingNotes
            );

            BigDecimal difference = closedRegister.getCashDifference();
            String differenceMsg = "";
            if (difference.compareTo(BigDecimal.ZERO) > 0) {
                differenceMsg = " (Sobrante: Q" + difference + ")";
            } else if (difference.compareTo(BigDecimal.ZERO) < 0) {
                differenceMsg = " (Faltante: Q" + difference.abs() + ")";
            }

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Caja cerrada",
                    "Caja cerrada exitosamente" + differenceMsg));

            // Refresh lists
            loadOpenRegisters();
            searchRegisters();

            PrimeFaces.current().executeScript("PF('dlgCloseRegister').hide()");
            PrimeFaces.current().ajax().update("form:messages", "form:open-registers", "form:dt-registers");

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al cerrar caja",
                    e.getMessage()));
        }
    }

    /**
     * View cash register details
     */
    public void viewRegisterDetails(CashRegister register) {
        selectedRegister = register;
    }

    /**
     * Check if a branch has an open register
     */
    public boolean branchHasOpenRegister(Branch branch) {
        return cashRegisterService.hasOpenRegister(branch);
    }

    /**
     * Get status label for display
     */
    public String getStatusLabel(CashRegisterStatus status) {
        switch (status) {
            case OPEN:
                return "Abierta";
            case CLOSED:
                return "Cerrada";
            default:
                return status.name();
        }
    }

    /**
     * Get status severity for UI styling
     */
    public String getStatusSeverity(CashRegisterStatus status) {
        switch (status) {
            case OPEN:
                return "success";
            case CLOSED:
                return "info";
            default:
                return "secondary";
        }
    }

    /**
     * Calculate expected cash for a register
     */
    public BigDecimal calculateExpectedCash(CashRegister register) {
        if (register == null) {
            return BigDecimal.ZERO;
        }
        return cashRegisterService.calculateExpectedCash(register);
    }

    /**
     * Format currency for display
     */
    public String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "Q0.00";
        }
        return "Q" + amount.setScale(2, java.math.RoundingMode.HALF_UP).toString();
    }
}
