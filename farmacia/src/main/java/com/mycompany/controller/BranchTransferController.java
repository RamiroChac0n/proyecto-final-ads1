package com.mycompany.controller;

import com.mycompany.model.entity.*;
import com.mycompany.model.entity.enums.TransferStatus;
import com.mycompany.repository.BranchRepository;
import com.mycompany.repository.ProductBatchRepository;
import com.mycompany.repository.ProductRepository;
import com.mycompany.service.IBranchTransferService;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSF Managed Bean controller for branch transfer operations (ADMIN only).
 * <p>
 * This view-scoped controller manages product transfers between branches with
 * a two-step approval workflow (PENDING → IN_TRANSIT → COMPLETED).
 * Only users with ADMIN role can request, approve, and receive transfers.
 * </p>
 *
 * <h3>Core Functionality:</h3>
 * <ul>
 *   <li><strong>Request Transfer:</strong> Creates a new transfer request with PENDING status</li>
 *   <li><strong>Approve Transfer:</strong> Approves PENDING transfer, changes to IN_TRANSIT, reduces stock at source</li>
 *   <li><strong>Receive Transfer:</strong> Receives IN_TRANSIT transfer, changes to COMPLETED, increases stock at destination</li>
 *   <li><strong>Cancel Transfer:</strong> Cancels PENDING or IN_TRANSIT transfers (reverses stock if needed)</li>
 * </ul>
 *
 * <h3>Transfer Workflow:</h3>
 * <ul>
 *   <li><strong>PENDING:</strong> Transfer requested, awaiting approval</li>
 *   <li><strong>IN_TRANSIT:</strong> Transfer approved, stock reduced at source, awaiting receipt</li>
 *   <li><strong>COMPLETED:</strong> Transfer received, stock added to destination</li>
 *   <li><strong>CANCELLED:</strong> Transfer cancelled (stock reversed if was IN_TRANSIT)</li>
 * </ul>
 *
 * <h3>Business Rules:</h3>
 * <ul>
 *   <li>Only ADMIN role can manage transfers</li>
 *   <li>Cannot transfer to the same branch</li>
 *   <li>Batch must be active and not expired</li>
 *   <li>Sufficient stock must be available</li>
 *   <li>Quantity must be positive</li>
 *   <li>Stock is FIFO-based (First In, First Out)</li>
 *   <li>Inventory movements are tracked automatically</li>
 * </ul>
 *
 * <h3>UI Features:</h3>
 * <ul>
 *   <li>Transfer history table with status filters</li>
 *   <li>Dialog for creating new transfer (select product, batch, destination, quantity)</li>
 *   <li>Status-based action buttons (Approve, Receive, Cancel)</li>
 *   <li>Status badges with color coding</li>
 *   <li>Details view showing full transfer information</li>
 *   <li>Date range filtering</li>
 * </ul>
 *
 * @author ramir
 * @version 1.0
 * @see BranchTransfer
 * @see IBranchTransferService
 * @see TransferStatus
 */
@Data
@Named(value = "branchTransferController")
@ViewScoped
public class BranchTransferController implements Serializable {

    @EJB
    private IBranchTransferService transferService;

    @EJB
    private BranchRepository branchRepository;

    @EJB
    private ProductRepository productRepository;

    @EJB
    private ProductBatchRepository batchRepository;

    @Inject
    private UserController userController;

    // Transfer fields
    private BranchTransfer selectedTransfer;
    private List<BranchTransfer> transfers;
    private BranchTransfer newTransfer;

    // For creating new transfer
    private Long selectedProductId;
    private Integer selectedBatchId;
    private Integer destinationBranchId;
    private Integer quantity;
    private String notes;

    // For cancelling
    private String cancellationReason;

    // Search filters
    private TransferStatus selectedStatus;
    private LocalDate fromDate;
    private LocalDate toDate;

    // Available data for dropdowns
    private List<Branch> branches;
    private List<Product> products;
    private List<ProductBatch> availableBatches;

    /**
     * Initializes the controller after dependency injection.
     * Loads branches, initializes date range, and loads transfers.
     */
    @PostConstruct
    public void init() {
        checkAdminAccess();
        loadBranches();
        initializeDateRange();
        loadTransfers();
    }

    /**
     * Checks if the current user has ADMIN role.
     * Redirects to home if not authorized.
     */
    public void checkAdminAccess() {
        if (!userController.isLoggedIn() || !userController.isAdmin()) {
            try {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "No tiene permisos para acceder a esta funcionalidad"));
                FacesContext.getCurrentInstance().getExternalContext().redirect("home.xhtml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads all active branches for dropdowns.
     */
    public void loadBranches() {
        branches = branchRepository.findAll().stream()
                .filter(Branch::getIsActive)
                .collect(Collectors.toList());
    }

    /**
     * Loads all active products for the product dropdown.
     */
    public void loadProducts() {
        products = productRepository.findAll().stream()
                .filter(Product::getIsActive)
                .collect(Collectors.toList());
    }

    /**
     * Loads available batches for the selected product and user's branch.
     * Called when a product is selected in the create transfer dialog.
     */
    public void onProductSelect() {
        if (selectedProductId != null) {
            Product product = productRepository.findById(selectedProductId);
            if (product != null) {
                // Get user's branch
                Branch userBranch = userController.getCurrentUser().getBranch();
                if (userBranch != null) {
                    // Load available batches for this product at user's branch using FIFO
                    availableBatches = batchRepository.findAvailableBatchesByProductAndBranchFIFO(product, userBranch);
                } else {
                    availableBatches = batchRepository.findAvailableBatchesByProductFIFO(product);
                }
            }
        } else {
            availableBatches = null;
        }
    }

    /**
     * Initializes date range filter to last 30 days.
     */
    private void initializeDateRange() {
        toDate = LocalDate.now();
        fromDate = toDate.minusDays(30);
    }

    /**
     * Loads all transfers (can be filtered later).
     */
    public void loadTransfers() {
        transfers = transferService.list();
    }

    /**
     * Prepares the dialog for creating a new transfer.
     */
    public void prepareNewTransfer() {
        newTransfer = new BranchTransfer();
        selectedProductId = null;
        selectedBatchId = null;
        destinationBranchId = null;
        quantity = null;
        notes = null;
        availableBatches = null;
        loadProducts();
    }

    /**
     * Creates a new transfer request.
     */
    public void createTransfer() {
        try {
            // Validate inputs
            if (selectedProductId == null || selectedBatchId == null || destinationBranchId == null || quantity == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "Todos los campos son obligatorios"));
                return;
            }

            // Validate quantity is positive
            if (quantity <= 0) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "La cantidad debe ser mayor a cero"));
                return;
            }

            // Get entities
            Product product = productRepository.findById(selectedProductId);
            ProductBatch batch = batchRepository.findById(selectedBatchId);
            Branch fromBranch = userController.getCurrentUser().getBranch();
            Branch toBranch = branchRepository.findById(destinationBranchId);

            if (product == null || batch == null || fromBranch == null || toBranch == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "Datos inválidos. Por favor intente nuevamente."));
                return;
            }

            // Validate same branch
            if (fromBranch.getBranchId().equals(toBranch.getBranchId())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "La sucursal de origen y destino deben ser diferentes"));
                return;
            }

            // Validate product is active
            if (!product.getIsActive()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "El producto seleccionado está inactivo"));
                return;
            }

            // Validate batch is not expired
            if (batch.getIsExpired()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "El lote seleccionado está vencido"));
                return;
            }

            // Validate batch is active
            if (!batch.getIsActive()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "El lote seleccionado no está activo"));
                return;
            }

            // Validate sufficient stock
            if (batch.getQuantityAvailable() < quantity) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "Stock insuficiente. Disponible: " + batch.getQuantityAvailable() + " unidades"));
                return;
            }

            // Validate product-batch consistency
            if (!batch.getProduct().getProductId().equals(product.getProductId())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "El lote seleccionado no corresponde al producto"));
                return;
            }

            // Build transfer
            BranchTransfer transfer = BranchTransfer.builder()
                    .product(product)
                    .batch(batch)
                    .fromBranch(fromBranch)
                    .toBranch(toBranch)
                    .quantity(quantity)
                    .notes(notes)
                    .requestedBy(userController.getCurrentUser().getUserName())
                    .build();

            // Request transfer (this validates and creates with PENDING status)
            BranchTransfer createdTransfer = transferService.requestTransfer(transfer);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito",
                            "Traslado #" + createdTransfer.getTransferId() + " solicitado exitosamente. Pendiente de aprobación."));

            // Reload transfers
            loadTransfers();

            // Close dialog
            PrimeFaces.current().dialog().closeDynamic(null);

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            e.getMessage()));
        }
    }

    /**
     * Approves a PENDING transfer.
     */
    public void approveTransfer(BranchTransfer transfer) {
        try {
            transferService.approveTransfer(transfer.getTransferId(), userController.getCurrentUser());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito",
                            "Traslado #" + transfer.getTransferId() + " aprobado. En tránsito a " + transfer.getToBranch().getBranchName() + "."));

            loadTransfers();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            e.getMessage()));
        }
    }

    /**
     * Receives an IN_TRANSIT transfer.
     */
    public void receiveTransfer(BranchTransfer transfer) {
        try {
            transferService.receiveTransfer(transfer.getTransferId(), userController.getCurrentUser());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito",
                            "Traslado #" + transfer.getTransferId() + " recibido exitosamente. " + transfer.getQuantity() + " unidades agregadas al inventario."));

            loadTransfers();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            e.getMessage()));
        }
    }

    /**
     * Prepares the cancel dialog.
     */
    public void prepareCancelTransfer(BranchTransfer transfer) {
        selectedTransfer = transfer;
        cancellationReason = null;
    }

    /**
     * Cancels a transfer (PENDING or IN_TRANSIT).
     */
    public void cancelTransfer() {
        try {
            if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "Debe proporcionar una razón para cancelar el traslado"));
                return;
            }

            Integer transferId = selectedTransfer.getTransferId();
            transferService.cancelTransfer(
                    transferId,
                    cancellationReason,
                    userController.getCurrentUser()
            );

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito",
                            "Traslado #" + transferId + " cancelado exitosamente."));

            loadTransfers();

            // Close dialog
            PrimeFaces.current().dialog().closeDynamic(null);

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            e.getMessage()));
        }
    }

    /**
     * Views transfer details.
     */
    public void viewDetails(BranchTransfer transfer) {
        selectedTransfer = transfer;
    }

    /**
     * Gets status label in Spanish.
     */
    public String getStatusLabel(TransferStatus status) {
        if (status == null) return "";
        switch (status) {
            case PENDING:
                return "PENDIENTE";
            case IN_TRANSIT:
                return "EN TRÁNSITO";
            case COMPLETED:
                return "COMPLETADO";
            case CANCELLED:
                return "CANCELADO";
            default:
                return status.name();
        }
    }

    /**
     * Gets status severity for badge coloring.
     */
    public String getStatusSeverity(TransferStatus status) {
        if (status == null) return "secondary";
        switch (status) {
            case PENDING:
                return "warning";
            case IN_TRANSIT:
                return "info";
            case COMPLETED:
                return "success";
            case CANCELLED:
                return "danger";
            default:
                return "secondary";
        }
    }

    /**
     * Checks if transfer can be approved.
     */
    public boolean canApprove(BranchTransfer transfer) {
        return transfer.getStatus() == TransferStatus.PENDING;
    }

    /**
     * Checks if transfer can be received.
     */
    public boolean canReceive(BranchTransfer transfer) {
        return transfer.getStatus() == TransferStatus.IN_TRANSIT;
    }

    /**
     * Checks if transfer can be cancelled.
     */
    public boolean canCancel(BranchTransfer transfer) {
        return transfer.getStatus() == TransferStatus.PENDING ||
               transfer.getStatus() == TransferStatus.IN_TRANSIT;
    }

    /**
     * Converts LocalDate to Date for filtering.
     */
    private Date toDate(LocalDate localDate) {
        return localDate == null ? null :
            Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Gets all possible transfer statuses for filter dropdown.
     */
    public TransferStatus[] getTransferStatuses() {
        return TransferStatus.values();
    }
}
