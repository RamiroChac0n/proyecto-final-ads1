package com.mycompany.controller;

import com.mycompany.model.dto.BatchAllocation;
import com.mycompany.model.entity.*;
import com.mycompany.model.entity.enums.SaleStatus;
import com.mycompany.repository.BranchRepository;
import com.mycompany.service.ICashRegisterService;
import com.mycompany.service.ICustomerService;
import com.mycompany.service.IProductService;
import com.mycompany.service.ISaleService;
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
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller for Sales operations
 * Accessible by ADMIN and CASHIER roles only
 * @author ramir
 */
@Data
@Named(value = "saleController")
@ViewScoped
public class SaleController implements Serializable {

    @EJB
    private ISaleService saleService;

    @EJB
    private IProductService productService;

    @EJB
    private ICashRegisterService cashRegisterService;

    @EJB
    private BranchRepository branchRepository;

    @EJB
    private ICustomerService customerService;

    @Inject
    private UserController userController;

    // Current sale being created
    private Sale currentSale;
    private List<SaleDetail> cartItems;

    // Product selection
    private List<Product> availableProducts;
    private Product selectedProduct;
    private Integer selectedQuantity;

    // Sale history
    private List<Sale> salesHistory;
    private Sale selectedSale;

    // Customer information
    private Customer selectedCustomer;
    private Customer newCustomer;
    private String customerName;
    private String customerNit;
    private String customerAddress;
    private String customerPhone;
    private boolean customerFound;

    // Payment
    private BigDecimal cashReceived;
    private BigDecimal changeGiven;

    // Filters
    private String productSearchText;

    // Available branches and cash registers
    private List<Branch> branches;
    private Integer selectedBranchId;
    private CashRegister selectedCashRegister;

    @PostConstruct
    public void init() {
        checkSalesAccess();
        initializeNewSale();
        loadAvailableProducts();
        loadBranches();
        loadSalesHistory();
    }

    /**
     * Check if current user has access to sales (ADMIN or CASHIER only)
     */
    public void checkSalesAccess() {
        if (!userController.isLoggedIn()) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (!userController.isAdminOrCashier()) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("home.xhtml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initialize a new sale
     */
    public void initializeNewSale() {
        currentSale = new Sale();
        cartItems = new ArrayList<>();
        customerNit = "C/F";
        customerName = "";
        customerAddress = "";
        customerPhone = "";
        cashReceived = BigDecimal.ZERO;
        changeGiven = BigDecimal.ZERO;
        selectedProduct = null;
        selectedQuantity = 1;
        selectedBranchId = null;
        selectedCashRegister = null;
    }

    /**
     * Load all active products
     */
    public void loadAvailableProducts() {
        availableProducts = productService.findActiveProducts();
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
     * Load sales history
     */
    public void loadSalesHistory() {
        salesHistory = saleService.list();
    }

    /**
     * Add product to cart
     */
    public void addToCart() {
        try {
            // Validate product selection
            if (selectedProduct == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Producto requerido", "Debe seleccionar un producto"));
                return;
            }

            // Validate quantity
            if (selectedQuantity == null || selectedQuantity <= 0) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Cantidad inválida", "La cantidad debe ser mayor a cero"));
                return;
            }

            // Check stock availability
            if (!saleService.validateStockAvailability(selectedProduct, selectedQuantity)) {
                Integer available = saleService.getTotalAvailableQuantity(selectedProduct);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Stock insuficiente",
                        "Solo hay " + available + " unidades disponibles de " + selectedProduct.getCommercialName()));
                return;
            }

            // Get batch allocation to show pricing
            List<BatchAllocation> allocations = saleService.allocateStock(selectedProduct, selectedQuantity);

            // Calculate weighted average price from allocations
            BigDecimal totalPrice = BigDecimal.ZERO;
            BigDecimal totalCost = BigDecimal.ZERO;
            for (BatchAllocation allocation : allocations) {
                totalPrice = totalPrice.add(allocation.getUnitPrice().multiply(new BigDecimal(allocation.getQuantity())));
                totalCost = totalCost.add(allocation.getUnitCost().multiply(new BigDecimal(allocation.getQuantity())));
            }
            BigDecimal avgPrice = totalPrice.divide(new BigDecimal(selectedQuantity), 2, RoundingMode.HALF_UP);
            BigDecimal avgCost = totalCost.divide(new BigDecimal(selectedQuantity), 2, RoundingMode.HALF_UP);

            // Create sale detail
            SaleDetail detail = SaleDetail.builder()
                .product(selectedProduct)
                .quantity(selectedQuantity)
                .unitPrice(avgPrice)
                .unitCost(avgCost)
                .discountPercentage(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .lineTotal(avgPrice.multiply(new BigDecimal(selectedQuantity)))
                .requiresPrescription(selectedProduct.getRequiresPrescription())
                .build();

            cartItems.add(detail);

            // Reset selection
            selectedProduct = null;
            selectedQuantity = 1;

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Producto agregado", "Producto agregado al carrito"));

            PrimeFaces.current().ajax().update("form:cart-table", "form:totals-panel", "form:messages");

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", e.getMessage()));
        }
    }

    /**
     * Remove item from cart
     */
    public void removeFromCart(SaleDetail detail) {
        cartItems.remove(detail);
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Producto eliminado", "Producto eliminado del carrito"));
        PrimeFaces.current().ajax().update("form:cart-table", "form:totals-panel", "form:messages");
    }

    /**
     * Process the sale
     */
    public void processSale() {
        try {
            // Validate cart not empty
            if (cartItems == null || cartItems.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Carrito vacío", "Debe agregar productos al carrito"));
                return;
            }

            // Validate branch and cash register selection
            if (selectedBranchId == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Sucursal requerida", "Debe seleccionar una sucursal"));
                return;
            }

            Branch branch = branchRepository.findById(selectedBranchId);
            if (branch == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error", "Sucursal no encontrada"));
                return;
            }

            // Get open cash register for branch
            CashRegister cashRegister = cashRegisterService.getOpenRegisterByBranch(branch);
            if (cashRegister == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Caja cerrada", "No hay caja abierta para esta sucursal. Un administrador debe abrir la caja primero."));
                return;
            }

            // Calculate totals with IVA
            BigDecimal total = calculateTotal(); // Total with IVA included
            BigDecimal subtotal = calculateSubtotalWithoutTax(); // Subtotal without IVA
            BigDecimal taxAmount = calculateTax(); // IVA amount (12%)

            // Validate customer information based on NIT
            if (!validateCustomerInformation()) {
                return; // Validation errors already added to FacesContext
            }

            // Validate payment
            if (cashReceived == null || cashReceived.compareTo(total) < 0) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Pago insuficiente",
                        "El efectivo recibido debe ser mayor o igual al total de la venta (Q" + total + ")"));
                return;
            }

            // Calculate change
            changeGiven = cashReceived.subtract(total);

            // Generate sale number
            String saleNumber = generateSaleNumber();

            // Build sale
            currentSale = Sale.builder()
                .saleNumber(saleNumber)
                .saleDate(new Date())
                .cashRegister(cashRegister)
                .user(userController.getCurrentUser())
                .branch(branch)
                .customer(selectedCustomer) // Associate customer if found
                .customerName(customerName != null && !customerName.trim().isEmpty() ? customerName : null)
                .customerNit(customerNit != null && !customerNit.trim().isEmpty() ? customerNit : "C/F")
                .customerAddress(customerAddress != null && !customerAddress.trim().isEmpty() ? customerAddress : null)
                .customerPhone(customerPhone != null && !customerPhone.trim().isEmpty() ? customerPhone : null)
                .subtotal(subtotal) // Subtotal WITHOUT IVA
                .discountPercentage(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(taxAmount) // IVA at 12%
                .totalAmount(total) // Total WITH IVA
                .cashReceived(cashReceived)
                .changeGiven(changeGiven)
                .saleStatus(SaleStatus.COMPLETED)
                .build();

            // Process sale through service (this will allocate inventory and save)
            Sale savedSale = saleService.processSale(currentSale, cartItems);

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Venta exitosa",
                    "Venta No. " + saleNumber + " procesada exitosamente. Cambio: Q" + changeGiven));

            // Reset for new sale
            initializeNewSale();
            loadSalesHistory();

            PrimeFaces.current().ajax().update("form:cart-table", "form:totals-panel",
                "form:customer-panel", "form:payment-panel", "form:messages");

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al procesar venta", e.getMessage()));
        }
    }

    /**
     * Cancel a sale
     */
    public void cancelSale(Sale sale) {
        try {
            User currentUser = userController.getCurrentUser();
            saleService.cancelSale(sale.getSaleId(), currentUser.getId(), "Cancelación solicitada por usuario");

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Venta cancelada", "La venta ha sido cancelada y el inventario restaurado"));

            loadSalesHistory();
            PrimeFaces.current().ajax().update("form:sales-history", "form:messages");

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al cancelar", e.getMessage()));
        }
    }

    /**
     * Calculate subtotal from cart items
     */
    public BigDecimal calculateSubtotal() {
        return cartItems.stream()
            .map(SaleDetail::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total (including tax/discounts if any)
     */
    public BigDecimal calculateTotal() {
        return calculateSubtotal(); // Returns total with IVA included
    }

    /**
     * Calculate subtotal WITHOUT IVA (prices include IVA)
     * IVA = 12%, so to get subtotal without tax: Total / 1.12
     */
    public BigDecimal calculateSubtotalWithoutTax() {
        BigDecimal total = calculateTotal();
        BigDecimal taxDivisor = new BigDecimal("1.12"); // 1 + 0.12
        return total.divide(taxDivisor, 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate IVA amount (12%)
     * IVA = Total - Subtotal without IVA
     */
    public BigDecimal calculateTax() {
        BigDecimal total = calculateTotal();
        BigDecimal subtotalWithoutTax = calculateSubtotalWithoutTax();
        return total.subtract(subtotalWithoutTax);
    }

    /**
     * Calculate change based on cash received and total amount
     * This method is called automatically when cash received changes
     */
    public void calculateChange() {
        BigDecimal total = calculateTotal();
        if (cashReceived != null && cashReceived.compareTo(BigDecimal.ZERO) > 0) {
            changeGiven = cashReceived.subtract(total);
        } else {
            changeGiven = BigDecimal.ZERO;
        }
    }

    /**
     * Update line total when quantity changes in cart
     * Validates stock availability and recalculates line total
     */
    public void updateLineTotal(SaleDetail item) {
        try {
            if (item != null && item.getQuantity() != null && item.getUnitPrice() != null) {
                // Validate stock availability
                if (!saleService.validateStockAvailability(item.getProduct(), item.getQuantity())) {
                    Integer available = saleService.getTotalAvailableQuantity(item.getProduct());
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Stock insuficiente",
                            "Solo hay " + available + " unidades disponibles de " + item.getProduct().getCommercialName()));
                    // Reset to previous valid quantity (or remove this line to keep the invalid value)
                    return;
                }

                // Recalculate line total
                BigDecimal discountAmount = item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO;
                BigDecimal lineTotal = item.getUnitPrice()
                    .multiply(new BigDecimal(item.getQuantity()))
                    .subtract(discountAmount);
                item.setLineTotal(lineTotal);

                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Cantidad actualizada",
                        "Total de línea recalculado"));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al actualizar", e.getMessage()));
        }
    }

    /**
     * Generate unique sale number
     */
    private String generateSaleNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "VEN-" + LocalDateTime.now().format(formatter);
    }

    /**
     * Get available quantity for a product
     */
    public Integer getAvailableQuantity(Product product) {
        if (product == null) {
            return 0;
        }
        return saleService.getTotalAvailableQuantity(product);
    }

    /**
     * Format currency for display
     */
    public String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "Q0.00";
        }
        return "Q" + amount.setScale(2, RoundingMode.HALF_UP).toString();
    }

    /**
     * Format date for display
     */
    public String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(date);
    }

    /**
     * Get status label
     */
    public String getStatusLabel(SaleStatus status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case COMPLETED:
                return "Completada";
            case CANCELLED:
                return "Cancelada";
            case PENDING:
                return "Pendiente";
            default:
                return status.name();
        }
    }

    /**
     * Get status severity for UI styling
     */
    public String getStatusSeverity(SaleStatus status) {
        if (status == null) {
            return "secondary";
        }
        switch (status) {
            case COMPLETED:
                return "success";
            case CANCELLED:
                return "danger";
            case PENDING:
                return "warning";
            default:
                return "secondary";
        }
    }

    /**
     * Check if sale can be cancelled
     */
    public boolean canCancelSale(Sale sale) {
        return sale != null &&
               SaleStatus.COMPLETED.equals(sale.getSaleStatus()) &&
               userController.isAdmin();
    }

    /**
     * Check if customer information is required based on NIT
     * If NIT is not "C/F", then customer information is required
     */
    public boolean isCustomerInfoRequired() {
        return customerNit != null &&
               !customerNit.trim().isEmpty() &&
               !"C/F".equalsIgnoreCase(customerNit.trim());
    }

    /**
     * Validate customer information when required
     * @return true if validation passes, false otherwise
     */
    private boolean validateCustomerInformation() {
        if (!isCustomerInfoRequired()) {
            // Customer info not required when NIT is "C/F"
            return true;
        }

        // When NIT is provided (not "C/F"), customer information is required
        boolean isValid = true;

        if (customerName == null || customerName.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Nombre requerido",
                    "El nombre del cliente es requerido cuando se proporciona un NIT específico"));
            isValid = false;
        }

        if (customerAddress == null || customerAddress.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Dirección requerida",
                    "La dirección del cliente es requerida cuando se proporciona un NIT específico"));
            isValid = false;
        }

        if (customerPhone == null || customerPhone.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Teléfono requerido",
                    "El teléfono del cliente es requerido cuando se proporciona un NIT específico"));
            isValid = false;
        }

        return isValid;
    }

    /**
     * View sale details
     * Loads sale with all relationships to avoid LazyInitializationException
     */
    public void viewSaleDetails(Sale sale) {
        // Load the complete sale with all details and relationships
        selectedSale = saleService.findByIdWithDetails(sale.getSaleId());
    }

    /**
     * Get open cash register for selected branch
     */
    public CashRegister getOpenCashRegister() {
        if (selectedBranchId == null) {
            return null;
        }
        Branch branch = branchRepository.findById(selectedBranchId);
        if (branch == null) {
            return null;
        }
        return cashRegisterService.getOpenRegisterByBranch(branch);
    }

    /**
     * Check if branch has open cash register
     */
    public boolean hasOpenCashRegister() {
        return getOpenCashRegister() != null;
    }

    // ==================== CUSTOMER MANAGEMENT METHODS ====================

    /**
     * Search customer by NIT (Tax ID)
     * Auto-fills customer information if found
     */
    public void searchCustomerByNit() {
        try {
            // Clear previous customer data
            clearCustomerDataExceptNit();

            if (customerNit == null || customerNit.trim().isEmpty() || "C/F".equalsIgnoreCase(customerNit.trim())) {
                // Default consumer
                selectedCustomer = null;
                customerFound = false;
                return;
            }

            // Search for customer
            selectedCustomer = customerService.findByTaxId(customerNit.trim());

            if (selectedCustomer != null) {
                // Customer found - auto-fill information
                customerName = selectedCustomer.getCustomerName();
                customerAddress = selectedCustomer.getAddress();
                customerPhone = selectedCustomer.getPhone();
                customerFound = true;

                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Cliente encontrado",
                        "Información del cliente cargada automáticamente"));

                PrimeFaces.current().ajax().update("form:customerName", "form:customerAddress",
                                                   "form:customerPhone", "form:customer-status",
                                                   "form:add-customer-btn");
            } else {
                // Customer not found
                customerFound = false;
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Cliente no encontrado",
                        "No existe un cliente con el NIT: " + customerNit + ". Puede agregarlo haciendo clic en 'Agregar Cliente'."));

                PrimeFaces.current().ajax().update("form:customerName", "form:customerAddress",
                                                   "form:customerPhone", "form:customer-status",
                                                   "form:add-customer-btn", "form:messages");
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al buscar cliente", e.getMessage()));
        }
    }

    /**
     * Search customer by phone
     * Auto-fills customer information if found
     */
    public void searchCustomerByPhone() {
        try {
            if (customerPhone == null || customerPhone.trim().isEmpty()) {
                return;
            }

            // Search for customer
            Customer customer = customerService.findByPhone(customerPhone.trim());

            if (customer != null) {
                // Customer found - auto-fill all information
                selectedCustomer = customer;
                customerNit = customer.getTaxId();
                customerName = customer.getCustomerName();
                customerAddress = customer.getAddress();
                customerFound = true;

                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Cliente encontrado",
                        "Información del cliente cargada automáticamente"));

                PrimeFaces.current().ajax().update("form:customer-panel");
            } else {
                // Customer not found
                customerFound = false;
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Cliente no encontrado",
                        "No existe un cliente con el teléfono: " + customerPhone));

                PrimeFaces.current().ajax().update("form:messages");
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al buscar cliente", e.getMessage()));
        }
    }

    /**
     * Open dialog to add new customer
     */
    public void openNewCustomerDialog() {
        // Initialize new customer with data from form
        newCustomer = Customer.builder()
            .taxId(customerNit != null && !"C/F".equalsIgnoreCase(customerNit) ? customerNit : "")
            .customerName(customerName != null ? customerName : "")
            .address(customerAddress != null ? customerAddress : "")
            .phone(customerPhone != null ? customerPhone : "")
            .isActive(true)
            .build();
        // Note: Dialog is shown via oncomplete in XHTML
    }

    /**
     * Save new customer and associate with current sale
     */
    public void saveNewCustomer() {
        try {
            // Validate required fields
            if (newCustomer.getTaxId() == null || newCustomer.getTaxId().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "NIT requerido", "Debe ingresar el NIT del cliente"));
                return;
            }

            if (newCustomer.getCustomerName() == null || newCustomer.getCustomerName().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Nombre requerido", "Debe ingresar el nombre del cliente"));
                return;
            }

            // Save customer
            Customer savedCustomer = customerService.save(newCustomer);

            // Auto-fill sale form with new customer data
            selectedCustomer = savedCustomer;
            customerNit = savedCustomer.getTaxId();
            customerName = savedCustomer.getCustomerName();
            customerAddress = savedCustomer.getAddress();
            customerPhone = savedCustomer.getPhone();
            customerFound = true;

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Cliente creado",
                    "Cliente agregado exitosamente: " + savedCustomer.getCustomerName()));

            PrimeFaces.current().executeScript("PF('newCustomerDialog').hide();");
            PrimeFaces.current().ajax().update("form:customer-panel", "form:messages");

        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error de validación", e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al guardar cliente", e.getMessage()));
        }
    }

    /**
     * Cancel new customer creation
     */
    public void cancelNewCustomer() {
        newCustomer = null;
        PrimeFaces.current().executeScript("PF('newCustomerDialog').hide();");
    }

    /**
     * Clear customer data except NIT
     */
    private void clearCustomerDataExceptNit() {
        selectedCustomer = null;
        customerName = "";
        customerAddress = "";
        customerPhone = "";
        customerFound = false;
    }

    /**
     * Clear all customer data
     */
    public void clearCustomerData() {
        selectedCustomer = null;
        customerNit = "C/F";
        customerName = "";
        customerAddress = "";
        customerPhone = "";
        customerFound = false;
    }
}
