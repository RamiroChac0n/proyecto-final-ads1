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
 * JSF Managed Bean controller for cash register operations (ADMIN only).
 * <p>
 * This view-scoped controller manages daily cash register operations including opening
 * registers with initial cash, closing registers with final cash count and reconciliation,
 * and viewing register history. Only users with ADMIN role can perform register operations.
 * </p>
 *
 * <h3>Core Functionality:</h3>
 * <ul>
 *   <li><strong>Open Register:</strong> Opens a new cash register for a branch with initial cash amount</li>
 *   <li><strong>Close Register:</strong> Closes an open register with final cash count and calculates difference</li>
 *   <li><strong>View History:</strong> Displays cash register history filtered by date range</li>
 *   <li><strong>View Details:</strong> Shows detailed information for a specific register</li>
 * </ul>
 *
 * <h3>Cash Reconciliation:</h3>
 * <ul>
 *   <li><strong>Expected Cash:</strong> Initial Cash + Total Sales</li>
 *   <li><strong>Cash Difference:</strong> Final Cash - Expected Cash</li>
 *   <li><strong>Surplus:</strong> Positive difference (more cash than expected)</li>
 *   <li><strong>Shortage:</strong> Negative difference (less cash than expected)</li>
 * </ul>
 *
 * <h3>Business Rules:</h3>
 * <ul>
 *   <li>Only ADMIN role can open and close registers</li>
 *   <li>Only one open register per branch at a time</li>
 *   <li>Initial cash must be ≥ 0</li>
 *   <li>Final cash must be ≥ 0</li>
 *   <li>Cash difference is calculated and displayed upon closing</li>
 *   <li>Closing notes are optional but recommended for documenting discrepancies</li>
 * </ul>
 *
 * <h3>UI Features:</h3>
 * <ul>
 *   <li>Currently Open Registers panel showing active registers</li>
 *   <li>Cash Register History table with date range filtering</li>
 *   <li>Dialog for opening register (select branch, enter initial cash)</li>
 *   <li>Dialog for closing register (enter final cash, optional notes)</li>
 *   <li>Details view with reconciliation information</li>
 *   <li>Status badges (OPEN=green, CLOSED=blue)</li>
 *   <li>Currency formatting for all monetary amounts</li>
 * </ul>
 *
 * @author ramir
 * @version 1.0
 * @see CashRegister
 * @see ICashRegisterService
 * @see CashRegisterStatus
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

    /**
     * Initializes the controller after dependency injection is complete.
     * <p>
     * This method performs the following initialization sequence:
     * </p>
     * <ol>
     *   <li>Loads all active branches for the branch selector dropdown</li>
     *   <li>Loads currently open registers for the "Open Registers" panel</li>
     *   <li>Initializes date range filter to last 30 days</li>
     *   <li>Searches and loads cash register history for initial display</li>
     * </ol>
     *
     * @see #loadBranches()
     * @see #loadOpenRegisters()
     * @see #initializeDateRange()
     * @see #searchRegisters()
     */
    @PostConstruct
    public void init() {
        loadBranches();
        loadOpenRegisters();
        initializeDateRange();
        searchRegisters();
    }

    /**
     * Loads all active branches from the database for the branch selector dropdown.
     * <p>
     * This method retrieves all branches and filters them to include only active branches
     * (isActive = true). The filtered list is used in the "Open Register" dialog to allow
     * administrators to select which branch the new register will be associated with.
     * </p>
     *
     * <h4>Side Effects:</h4>
     * <p>
     * Sets the {@code branches} field with the filtered list of active branches.
     * </p>
     *
     * @see Branch#getIsActive()
     * @see BranchRepository#findAll()
     */
    public void loadBranches() {
        branches = branchRepository.findAll().stream()
            .filter(Branch::getIsActive)
            .toList();
    }

    /**
     * Loads all currently open cash registers for display in the "Open Registers" panel.
     * <p>
     * This method queries the cash register history from the last 7 days and filters
     * to show only registers with {@link CashRegisterStatus#OPEN} status. This provides
     * administrators with a quick view of all registers currently accepting transactions.
     * </p>
     *
     * <h4>Query Parameters:</h4>
     * <ul>
     *   <li><strong>Date Range:</strong> Last 7 days from current date</li>
     *   <li><strong>Status Filter:</strong> Only OPEN registers</li>
     * </ul>
     *
     * <h4>Side Effects:</h4>
     * <p>
     * Sets the {@code openRegisters} field with the filtered list of open registers.
     * </p>
     *
     * @see ICashRegisterService#getRegisterHistory(LocalDate, LocalDate)
     * @see CashRegisterStatus#OPEN
     */
    public void loadOpenRegisters() {
        // Filter by user's branch
        Branch userBranch = userController.getCurrentUser().getBranch();
        if (userBranch != null) {
            openRegisters = cashRegisterService.getRegisterHistoryByBranch(
                userBranch,
                LocalDate.now().minusDays(7),
                LocalDate.now()
            ).stream()
            .filter(cr -> CashRegisterStatus.OPEN.equals(cr.getStatus()))
            .toList();
        } else {
            openRegisters = List.of(); // Empty list if no branch assigned
        }
    }

    /**
     * Initializes the date range filter to the last 30 days.
     * <p>
     * This method sets the default date range for the cash register history search:
     * </p>
     * <ul>
     *   <li><strong>From Date:</strong> 30 days before today</li>
     *   <li><strong>To Date:</strong> Today</li>
     * </ul>
     *
     * <h4>Side Effects:</h4>
     * <p>
     * Sets the {@code fromDate} and {@code toDate} fields to define the initial search range.
     * </p>
     */
    public void initializeDateRange() {
        toDate = LocalDate.now();
        fromDate = toDate.minusDays(30);
    }

    /**
     * Searches and loads cash register history filtered by date range.
     * <p>
     * This method queries the database for all cash registers within the specified
     * date range (fromDate to toDate). If either date is null, defaults are applied:
     * </p>
     * <ul>
     *   <li><strong>fromDate = null:</strong> Defaults to 30 days before today</li>
     *   <li><strong>toDate = null:</strong> Defaults to today</li>
     * </ul>
     *
     * <h4>UI Trigger:</h4>
     * <p>
     * This method is called:
     * </p>
     * <ul>
     *   <li>On initial page load (via {@link #init()})</li>
     *   <li>When user clicks "Search" button after changing date filters</li>
     *   <li>After opening or closing a register (to refresh the list)</li>
     * </ul>
     *
     * <h4>Side Effects:</h4>
     * <p>
     * Sets the {@code cashRegisters} field with the search results.
     * </p>
     *
     * @see ICashRegisterService#getRegisterHistory(LocalDate, LocalDate)
     */
    public void searchRegisters() {
        if (fromDate == null) {
            fromDate = LocalDate.now().minusDays(30);
        }
        if (toDate == null) {
            toDate = LocalDate.now();
        }

        // Filter by user's branch
        Branch userBranch = userController.getCurrentUser().getBranch();
        if (userBranch != null) {
            cashRegisters = cashRegisterService.getRegisterHistoryByBranch(userBranch, fromDate, toDate);
        } else {
            cashRegisters = List.of(); // Empty list if no branch assigned
        }
    }

    /**
     * Prepares the controller state for opening a new cash register.
     * <p>
     * This method initializes the form fields for the "Open Register" dialog and performs
     * preliminary validation to ensure only ADMIN users can proceed. It should be called
     * when the user clicks the "Open Register" button to display the dialog.
     * </p>
     *
     * <h4>Preparation Steps:</h4>
     * <ol>
     *   <li>Validates current user has ADMIN role</li>
     *   <li>Initializes {@code initialCash} to zero (admin will enter actual amount)</li>
     *   <li>TODO: Will auto-select branch based on user's assigned branch (future implementation)</li>
     * </ol>
     *
     * <h4>Validation:</h4>
     * <p>
     * If user is not ADMIN, displays error message and aborts dialog preparation.
     * </p>
     *
     * <h4>Side Effects:</h4>
     * <ul>
     *   <li>Sets {@code initialCash} to BigDecimal.ZERO</li>
     *   <li>May display error message if user lacks ADMIN role</li>
     * </ul>
     *
     * @see #openRegister()
     * @see UserController#isAdmin()
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
     * Opens a new cash register for the selected branch with the specified initial cash amount.
     * <p>
     * This method performs comprehensive validation and delegates the actual register creation
     * to the service layer. After successfully opening a register, it refreshes the UI panels
     * and closes the dialog.
     * </p>
     *
     * <h4>Validation Steps:</h4>
     * <ol>
     *   <li><strong>Role:</strong> Current user must have ADMIN role</li>
     *   <li><strong>Branch:</strong> selectedBranchId must not be null and must exist in database</li>
     *   <li><strong>Initial Cash:</strong> initialCash must not be null and must be ≥ 0</li>
     *   <li><strong>Business Rule:</strong> Branch must not already have an open register</li>
     * </ol>
     *
     * <h4>Success Flow:</h4>
     * <ol>
     *   <li>Calls {@link ICashRegisterService#openCashRegister(Branch, User, BigDecimal)}</li>
     *   <li>Displays success message with branch name</li>
     *   <li>Refreshes open registers panel</li>
     *   <li>Refreshes register history table</li>
     *   <li>Hides the "Open Register" dialog</li>
     *   <li>Updates UI components via AJAX</li>
     * </ol>
     *
     * <h4>Error Handling:</h4>
     * <p>
     * Catches all exceptions and displays error messages to the user. Validation errors
     * are thrown as {@link IllegalArgumentException} with user-friendly Spanish messages.
     * </p>
     *
     * @see ICashRegisterService#openCashRegister(Branch, User, BigDecimal)
     * @see #prepareOpenRegister()
     * @see #loadOpenRegisters()
     * @see #searchRegisters()
     */
    public void openRegister() {
        try {
            User admin = userController.getCurrentUser();

            // Validate admin role (service will also validate)
            if (!userController.isAdmin()) {
                throw new IllegalArgumentException("Solo administradores pueden abrir cajas");
            }

            // Validate initial cash
            if (initialCash == null || initialCash.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El monto inicial debe ser mayor o igual a cero");
            }

            // Use admin's assigned branch (users can only operate in their branch)
            Branch branch = admin.getBranch();
            if (branch == null) {
                throw new IllegalArgumentException("Usuario no tiene sucursal asignada. Contacte al administrador del sistema.");
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
     * Prepares the controller state for closing an open cash register.
     * <p>
     * This method initializes the form fields for the "Close Register" dialog, performs
     * preliminary validation, and calculates the expected cash amount for reference. It
     * should be called when the user clicks the "Close" button on an open register.
     * </p>
     *
     * <h4>Preparation Steps:</h4>
     * <ol>
     *   <li>Validates current user has ADMIN role</li>
     *   <li>Sets {@code selectedRegister} to the register being closed</li>
     *   <li>Initializes {@code finalCash} to zero (admin will enter actual counted amount)</li>
     *   <li>Initializes {@code closingNotes} to empty string</li>
     *   <li>Calculates expected cash (initial + total sales) for admin reference</li>
     * </ol>
     *
     * <h4>Validation:</h4>
     * <p>
     * If user is not ADMIN, displays error message and aborts dialog preparation.
     * </p>
     *
     * <h4>Side Effects:</h4>
     * <ul>
     *   <li>Sets {@code selectedRegister} to the register being closed</li>
     *   <li>Sets {@code finalCash} to BigDecimal.ZERO</li>
     *   <li>Sets {@code closingNotes} to empty string</li>
     *   <li>Calculates expected cash (for display purposes only, not stored)</li>
     *   <li>May display error message if user lacks ADMIN role</li>
     * </ul>
     *
     * @param register The {@link CashRegister} to be closed
     * @see #closeRegister()
     * @see ICashRegisterService#calculateExpectedCash(CashRegister)
     * @see UserController#isAdmin()
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
     * Closes the selected cash register with the entered final cash amount and reconciliation notes.
     * <p>
     * This method performs comprehensive validation, delegates the closing operation to the service
     * layer, calculates and displays the cash difference (surplus or shortage), and refreshes the UI.
     * </p>
     *
     * <h4>Validation Steps:</h4>
     * <ol>
     *   <li><strong>Role:</strong> Current user must have ADMIN role</li>
     *   <li><strong>Register:</strong> selectedRegister must not be null</li>
     *   <li><strong>Final Cash:</strong> finalCash must not be null and must be ≥ 0</li>
     * </ol>
     *
     * <h4>Success Flow:</h4>
     * <ol>
     *   <li>Calls {@link ICashRegisterService#closeCashRegister(Integer, User, BigDecimal, String)}</li>
     *   <li>Calculates cash difference: finalCash - (initialCash + totalSales)</li>
     *   <li>Displays success message with cash difference information:
     *     <ul>
     *       <li><strong>Positive:</strong> "Sobrante: Q##.##" (surplus)</li>
     *       <li><strong>Negative:</strong> "Faltante: Q##.##" (shortage, shown as absolute value)</li>
     *       <li><strong>Zero:</strong> No difference message (perfect reconciliation)</li>
     *     </ul>
     *   </li>
     *   <li>Refreshes open registers panel</li>
     *   <li>Refreshes register history table</li>
     *   <li>Hides the "Close Register" dialog</li>
     *   <li>Updates UI components via AJAX</li>
     * </ol>
     *
     * <h4>Error Handling:</h4>
     * <p>
     * Catches all exceptions and displays error messages to the user. Validation errors
     * are thrown as {@link IllegalArgumentException} with user-friendly Spanish messages.
     * </p>
     *
     * @see ICashRegisterService#closeCashRegister(Integer, User, BigDecimal, String)
     * @see #prepareCloseRegister(CashRegister)
     * @see #loadOpenRegisters()
     * @see #searchRegisters()
     * @see CashRegister#getCashDifference()
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
     * Sets the selected register for viewing details in a dialog or panel.
     * <p>
     * This method is called when the user clicks to view the full details of a
     * specific cash register. It sets the {@code selectedRegister} field which is
     * then bound to a details view component in the XHTML.
     * </p>
     *
     * @param register The {@link CashRegister} to display details for
     */
    public void viewRegisterDetails(CashRegister register) {
        selectedRegister = register;
    }

    /**
     * Checks if the specified branch currently has an open cash register.
     * <p>
     * This method is used in the UI to:
     * </p>
     * <ul>
     *   <li>Disable opening a new register if the branch already has one open</li>
     *   <li>Display warning indicators on branches with open registers</li>
     *   <li>Enforce the business rule: only one open register per branch at a time</li>
     * </ul>
     *
     * @param branch The {@link Branch} to check
     * @return {@code true} if the branch has an open register, {@code false} otherwise
     * @see ICashRegisterService#hasOpenRegister(Branch)
     */
    public boolean branchHasOpenRegister(Branch branch) {
        return cashRegisterService.hasOpenRegister(branch);
    }

    /**
     * Returns a user-friendly Spanish label for the cash register status.
     * <p>
     * This method translates the {@link CashRegisterStatus} enum values into
     * Spanish text suitable for display in the UI.
     * </p>
     *
     * <h4>Status Labels:</h4>
     * <ul>
     *   <li><strong>OPEN:</strong> "Abierta" (Open/Active)</li>
     *   <li><strong>CLOSED:</strong> "Cerrada" (Closed)</li>
     *   <li><strong>Unknown:</strong> Returns enum name as fallback</li>
     * </ul>
     *
     * @param status The {@link CashRegisterStatus} to get a label for
     * @return Spanish status label string
     * @see CashRegisterStatus
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
     * Returns the PrimeFaces badge severity for styling the cash register status.
     * <p>
     * This method maps {@link CashRegisterStatus} enum values to PrimeFaces severity
     * levels, which determine the badge color in the UI.
     * </p>
     *
     * <h4>Severity Mapping:</h4>
     * <ul>
     *   <li><strong>OPEN:</strong> "success" (green badge - register is active)</li>
     *   <li><strong>CLOSED:</strong> "info" (blue badge - register is closed)</li>
     *   <li><strong>Unknown:</strong> "secondary" (gray badge)</li>
     * </ul>
     *
     * <h4>Usage in XHTML:</h4>
     * <pre>
     * &lt;p:badge value="#{cashRegisterController.getStatusLabel(register.status)}"
     *          severity="#{cashRegisterController.getStatusSeverity(register.status)}" /&gt;
     * </pre>
     *
     * @param status The {@link CashRegisterStatus} to get severity for
     * @return PrimeFaces severity string ("success", "info", or "secondary")
     * @see CashRegisterStatus
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
     * Calculates the expected cash amount for a register (initial cash + total sales).
     * <p>
     * This is a convenience method that delegates to the service layer. The expected
     * cash calculation is used during register closing to compare against the actual
     * final cash count and determine if there is a surplus or shortage.
     * </p>
     *
     * <h4>Calculation Formula:</h4>
     * <pre>
     * Expected Cash = Initial Cash + Total Sales
     * </pre>
     *
     * @param register The {@link CashRegister} to calculate expected cash for
     * @return Expected cash amount, or {@link BigDecimal#ZERO} if register is null
     * @see ICashRegisterService#calculateExpectedCash(CashRegister)
     * @see CashRegister#getInitialCash()
     * @see CashRegister#getTotalSales()
     */
    public BigDecimal calculateExpectedCash(CashRegister register) {
        if (register == null) {
            return BigDecimal.ZERO;
        }
        return cashRegisterService.calculateExpectedCash(register);
    }

    /**
     * Formats a monetary amount as Guatemalan Quetzales with 2 decimal places.
     * <p>
     * This method formats {@link BigDecimal} amounts into the Guatemalan currency format
     * with the symbol "Q" followed by the amount rounded to 2 decimal places.
     * </p>
     *
     * <h4>Examples:</h4>
     * <ul>
     *   <li>formatCurrency(BigDecimal.valueOf(1234.567)) → "Q1234.57"</li>
     *   <li>formatCurrency(BigDecimal.ZERO) → "Q0.00"</li>
     *   <li>formatCurrency(null) → "Q0.00"</li>
     * </ul>
     *
     * @param amount The {@link BigDecimal} amount to format, may be null
     * @return Formatted currency string (e.g., "Q1234.57"), defaults to "Q0.00" if amount is null
     */
    public String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "Q0.00";
        }
        return "Q" + amount.setScale(2, java.math.RoundingMode.HALF_UP).toString();
    }
}
