package com.mycompany.controller;

import com.mycompany.model.entity.*;
import com.mycompany.model.entity.enums.PurchaseOrderStatus;
import com.mycompany.model.entity.enums.PurchaseReceiptStatus;
import com.mycompany.service.IPurchaseOrderService;
import com.mycompany.service.IPurchaseReceiptService;
import com.mycompany.service.IProductService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSF Controller for Purchase Receipt management.
 * Handles receiving products from purchase orders and creating inventory batches.
 * Accessible by ADMIN and STOREKEEPER users.
 *
 * @author ramir
 */
@Data
@Named(value = "purchaseReceiptController")
@ViewScoped
public class PurchaseReceiptController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private IPurchaseReceiptService receiptService;

    @EJB
    private IPurchaseOrderService orderService;

    @EJB
    private IProductService productService;

    @Inject
    private UserController userController;

    // Current receipt being created/edited
    private PurchaseReceipt currentReceipt;
    private List<PurchaseReceipt> purchaseReceipts;

    // Receipt details management
    private List<PurchaseReceiptDetail> currentDetails;
    private PurchaseReceiptDetail newDetail;

    // Selected order for starting receipt
    private PurchaseOrder selectedOrder;
    private List<PurchaseOrder> availableOrders;

    // Products from selected order
    private List<PurchaseOrderDetail> orderProducts;
    private PurchaseOrderDetail selectedOrderProduct;

    // Filters
    private PurchaseReceiptStatus filterStatus;
    private List<PurchaseReceiptStatus> statusList;

    // Selected receipt for viewing/actions
    private PurchaseReceipt selectedReceipt;

    // Supplier invoice info
    private String supplierInvoiceNumber;
    private LocalDate supplierInvoiceDate;

    // Completion notes
    private String completionNotes;
    private Boolean markAsFinal;

    @PostConstruct
    public void init() {
        checkAccess();
        loadPurchaseReceipts();
        loadAvailableOrders();
        initializeStatusList();

        // Check for receiptId or orderId parameters
        FacesContext context = FacesContext.getCurrentInstance();
        String receiptIdParam = context.getExternalContext().getRequestParameterMap().get("receiptId");
        String orderIdParam = context.getExternalContext().getRequestParameterMap().get("orderId");

        if (receiptIdParam != null && !receiptIdParam.isEmpty()) {
            try {
                Integer receiptId = Integer.parseInt(receiptIdParam);
                PurchaseReceipt receipt = receiptService.findByIdWithDetails(receiptId);
                if (receipt != null) {
                    edit(receipt);
                } else {
                    showErrorMessage("Recepción no encontrada");
                }
            } catch (NumberFormatException e) {
                showErrorMessage("ID de recepción inválido");
            }
        } else if (orderIdParam != null && !orderIdParam.isEmpty()) {
            try {
                Integer orderId = Integer.parseInt(orderIdParam);
                PurchaseOrder order = orderService.findByIdWithDetails(orderId);
                if (order != null) {
                    // Set selected order
                    selectedOrder = order;
                    supplierInvoiceNumber = null;
                    supplierInvoiceDate = null;

                    // Start receipt directly (avoid redirect from @PostConstruct)
                    currentReceipt = receiptService.startReceipt(
                            selectedOrder.getOrderId(),
                            userController.getUser().getId(),
                            selectedOrder.getBranch().getBranchId(),
                            supplierInvoiceNumber,
                            supplierInvoiceDate
                    );

                    // Initialize details list with all products from order
                    currentDetails = new ArrayList<>();
                    orderProducts = new ArrayList<>(selectedOrder.getOrderDetails());

                    // Pre-create receipt details for each product in the order
                    for (PurchaseOrderDetail orderDetail : orderProducts) {
                        PurchaseReceiptDetail receiptDetail = PurchaseReceiptDetail.builder()
                                .product(orderDetail.getProduct())
                                .purchaseOrderDetail(orderDetail)
                                .quantityReceived(0) // Initialize to 0, user will fill
                                .quantityDamaged(0)
                                .unitCost(orderDetail.getUnitCost())
                                .salePrice(orderDetail.getExpectedSalePrice() != null
                                        ? orderDetail.getExpectedSalePrice()
                                        : orderDetail.getUnitCost().multiply(new BigDecimal("1.5")))
                                .batchNumber("") // User will fill
                                .build();
                        currentDetails.add(receiptDetail);
                    }

                    prepareNewDetail();

                    showSuccessMessage("Recepción iniciada: " + currentReceipt.getReceiptNumber());
                } else {
                    showErrorMessage("Orden no encontrada");
                }
            } catch (NumberFormatException e) {
                showErrorMessage("ID de orden inválido");
            } catch (Exception e) {
                showErrorMessage("Error al iniciar recepción: " + e.getMessage());
            }
        }

        // Initialize structures to avoid NPE
        if (currentDetails == null) {
            currentDetails = new ArrayList<>();
        }
        if (newDetail == null) {
            prepareNewDetail();
        }
    }

    /**
     * Load all purchase receipts from database.
     */
    private void loadPurchaseReceipts() {
        try {
            purchaseReceipts = receiptService.list();
        } catch (Exception e) {
            showErrorMessage("Error al cargar recepciones: " + e.getMessage());
        }
    }

    /**
     * Load orders that can have receipts started.
     */
    private void loadAvailableOrders() {
        try {
            availableOrders = orderService.list().stream()
                    .filter(order -> receiptService.canStartReceiptForOrder(order.getOrderId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            showErrorMessage("Error al cargar órdenes disponibles: " + e.getMessage());
        }
    }

    /**
     * Initialize status list for filter dropdown.
     */
    private void initializeStatusList() {
        statusList = Arrays.asList(PurchaseReceiptStatus.values());
    }

    /**
     * Prepare to start a new receipt from an order.
     */
    public void prepareStartReceipt(PurchaseOrder order) {
        selectedOrder = order;
        supplierInvoiceNumber = null;
        supplierInvoiceDate = null;
        orderProducts = new ArrayList<>(order.getOrderDetails());
    }

    /**
     * Start a new receipt for the selected order.
     */
    public String startReceipt() {
        try {
            if (selectedOrder == null) {
                showWarningMessage("Debe seleccionar una orden");
                return null;
            }

            // Start receipt
            currentReceipt = receiptService.startReceipt(
                    selectedOrder.getOrderId(),
                    userController.getUser().getId(),
                    selectedOrder.getBranch().getBranchId(),
                    supplierInvoiceNumber,
                    supplierInvoiceDate
            );

            // Initialize details list with all products from order
            currentDetails = new ArrayList<>();
            orderProducts = new ArrayList<>(selectedOrder.getOrderDetails());

            // Pre-create receipt details for each product in the order
            for (PurchaseOrderDetail orderDetail : orderProducts) {
                PurchaseReceiptDetail receiptDetail = PurchaseReceiptDetail.builder()
                        .product(orderDetail.getProduct())
                        .purchaseOrderDetail(orderDetail)
                        .quantityReceived(0) // Initialize to 0, user will fill
                        .quantityDamaged(0)
                        .unitCost(orderDetail.getUnitCost())
                        .salePrice(orderDetail.getExpectedSalePrice() != null
                                ? orderDetail.getExpectedSalePrice()
                                : orderDetail.getUnitCost().multiply(new BigDecimal("1.5")))
                        .batchNumber("") // User will fill
                        .build();
                currentDetails.add(receiptDetail);
            }

            prepareNewDetail();

            showSuccessMessage("Recepción iniciada: " + currentReceipt.getReceiptNumber());

            // Navigate to form
            return "purchase-receipt-form.xhtml?faces-redirect=true&receiptId=" + currentReceipt.getReceiptId();

        } catch (Exception e) {
            showErrorMessage("Error al iniciar recepción: " + e.getMessage());
            return null;
        }
    }

    /**
     * Prepare a new detail for adding to receipt.
     */
    private void prepareNewDetail() {
        newDetail = PurchaseReceiptDetail.builder()
                .quantityReceived(0)
                .quantityDamaged(0)
                .unitCost(BigDecimal.ZERO)
                .salePrice(BigDecimal.ZERO)
                .build();
        selectedOrderProduct = null;
    }

    /**
     * When user selects a product from order, pre-fill detail info.
     */
    public void onProductSelect() {
        if (selectedOrderProduct != null) {
            newDetail.setProduct(selectedOrderProduct.getProduct());
            newDetail.setUnitCost(selectedOrderProduct.getUnitCost());

            // Pre-fill expected sale price if available
            if (selectedOrderProduct.getExpectedSalePrice() != null) {
                newDetail.setSalePrice(selectedOrderProduct.getExpectedSalePrice());
            } else {
                // Default markup of 50%
                newDetail.setSalePrice(selectedOrderProduct.getUnitCost().multiply(new BigDecimal("1.5")));
            }

            newDetail.setPurchaseOrderDetail(selectedOrderProduct);
        }
    }

    /**
     * Add current product to receipt details.
     */
    public void addProductToReceipt() {
        try {
            // Validate
            if (selectedOrderProduct == null) {
                showWarningMessage("Debe seleccionar un producto");
                return;
            }

            if (newDetail.getBatchNumber() == null || newDetail.getBatchNumber().trim().isEmpty()) {
                showWarningMessage("El número de lote es requerido");
                return;
            }

            if (newDetail.getQuantityReceived() == null || newDetail.getQuantityReceived() <= 0) {
                showWarningMessage("La cantidad recibida debe ser mayor a 0");
                return;
            }

            if (newDetail.getQuantityDamaged() != null &&
                newDetail.getQuantityDamaged() > newDetail.getQuantityReceived()) {
                showWarningMessage("La cantidad dañada no puede ser mayor a la recibida");
                return;
            }

            if (newDetail.getExpirationDate() == null) {
                showWarningMessage("La fecha de vencimiento es requerida");
                return;
            }

            // Check if product already in details with same batch
            boolean alreadyExists = currentDetails.stream()
                    .anyMatch(d -> d.getProduct() != null &&
                                 d.getProduct().getProductId().equals(selectedOrderProduct.getProduct().getProductId()) &&
                                 d.getBatchNumber().equals(newDetail.getBatchNumber()));

            if (alreadyExists) {
                showWarningMessage("Este producto con el mismo lote ya está agregado");
                return;
            }

            // Warn if received less than ordered
            if (newDetail.getQuantityReceived() < selectedOrderProduct.getQuantityOrdered()) {
                showInfoMessage(String.format("Advertencia: Se recibieron %d de %d unidades ordenadas",
                        newDetail.getQuantityReceived(),
                        selectedOrderProduct.getQuantityOrdered()));
            }

            // Warn if there are damaged products
            if (newDetail.getQuantityDamaged() != null && newDetail.getQuantityDamaged() > 0) {
                showWarningMessage(String.format("Atención: %d unidades llegaron dañadas",
                        newDetail.getQuantityDamaged()));
            }

            // Add to current details
            currentDetails.add(newDetail);

            // Prepare for next product
            prepareNewDetail();

            showSuccessMessage("Producto agregado correctamente");

        } catch (Exception e) {
            showErrorMessage("Error al agregar producto: " + e.getMessage());
        }
    }

    /**
     * Remove a product from receipt details.
     */
    public void removeProductFromReceipt(PurchaseReceiptDetail detail) {
        currentDetails.remove(detail);
        showInfoMessage("Producto eliminado");
    }

    /**
     * Save the current receipt as draft.
     */
    public String saveDraft() {
        try {
            if (currentDetails.isEmpty()) {
                showWarningMessage("No hay productos en la recepción");
                return null;
            }

            // Validate that at least one product has been properly filled in
            boolean hasValidProduct = false;
            for (PurchaseReceiptDetail detail : currentDetails) {
                if (detail.getQuantityReceived() != null && detail.getQuantityReceived() > 0 &&
                    detail.getBatchNumber() != null && !detail.getBatchNumber().trim().isEmpty()) {
                    hasValidProduct = true;
                    break;
                }
            }

            if (!hasValidProduct) {
                showWarningMessage("Debe completar la información de al menos un producto (cantidad recibida y número de lote)");
                return null;
            }

            // Clear existing details and add only valid ones (with data filled)
            currentReceipt.getReceiptDetails().clear();
            for (PurchaseReceiptDetail detail : currentDetails) {
                // Only add details that have been at least partially filled
                if (detail.getQuantityReceived() != null && detail.getQuantityReceived() > 0) {
                    currentReceipt.addDetail(detail);
                }
            }

            // Save or update
            if (currentReceipt.getReceiptId() == null) {
                currentReceipt = receiptService.save(currentReceipt);
                showSuccessMessage("Recepción guardada como borrador");
            } else {
                currentReceipt = receiptService.edit(currentReceipt);
                showSuccessMessage("Recepción actualizada");
            }

            return null; // Stay on same page

        } catch (Exception e) {
            showErrorMessage("Error al guardar: " + e.getMessage());
            return null;
        }
    }

    /**
     * Complete the receipt and create batches.
     */
    public String completeReceipt() {
        try {
            if (currentDetails.isEmpty()) {
                showWarningMessage("No hay productos en la recepción");
                return null;
            }

            // Validate all products that will be saved
            StringBuilder validationErrors = new StringBuilder();
            int validProducts = 0;

            for (PurchaseReceiptDetail detail : currentDetails) {
                // Skip products with no quantity received
                if (detail.getQuantityReceived() == null || detail.getQuantityReceived() <= 0) {
                    continue;
                }

                validProducts++;

                // Validate required fields for products being received
                if (detail.getBatchNumber() == null || detail.getBatchNumber().trim().isEmpty()) {
                    validationErrors.append("- ").append(detail.getProduct().getCommercialName())
                            .append(": falta número de lote\n");
                }

                if (detail.getExpirationDate() == null) {
                    validationErrors.append("- ").append(detail.getProduct().getCommercialName())
                            .append(": falta fecha de vencimiento\n");
                }

                if (detail.getQuantityDamaged() != null && detail.getQuantityDamaged() > detail.getQuantityReceived()) {
                    validationErrors.append("- ").append(detail.getProduct().getCommercialName())
                            .append(": cantidad dañada no puede ser mayor a la recibida\n");
                }
            }

            if (validProducts == 0) {
                showWarningMessage("Debe completar la información de al menos un producto (cantidad recibida, número de lote y fecha de vencimiento)");
                return null;
            }

            if (validationErrors.length() > 0) {
                showErrorMessage("Por favor corrija los siguientes errores:\n" + validationErrors.toString());
                return null;
            }

            // First save the details (only those with quantity received > 0)
            currentReceipt.getReceiptDetails().clear();
            for (PurchaseReceiptDetail detail : currentDetails) {
                if (detail.getQuantityReceived() != null && detail.getQuantityReceived() > 0) {
                    currentReceipt.addDetail(detail);
                }
            }

            if (currentReceipt.getReceiptId() == null) {
                currentReceipt = receiptService.save(currentReceipt);
            } else {
                currentReceipt = receiptService.edit(currentReceipt);
            }

            // Now complete it
            currentReceipt = receiptService.completeReceipt(
                    currentReceipt.getReceiptId(),
                    userController.getUser().getId(),
                    markAsFinal,
                    completionNotes
            );

            showSuccessMessage("Recepción completada exitosamente. Los lotes han sido creados.");

            // Redirect to list
            return returnToList();

        } catch (Exception e) {
            showErrorMessage("Error al completar recepción: " + e.getMessage());
            return null;
        }
    }

    /**
     * Edit an existing receipt.
     */
    public void edit(PurchaseReceipt receipt) {
        if (receipt == null) {
            return;
        }

        try {
            if (!receiptService.canEdit(receipt)) {
                showWarningMessage("Solo se pueden editar recepciones en estado BORRADOR");
                return;
            }

            currentReceipt = receipt;
            selectedOrder = receipt.getPurchaseOrder();

            // Load order with details using separate query to avoid LazyInitializationException
            if (selectedOrder != null) {
                selectedOrder = orderService.findByIdWithDetails(selectedOrder.getOrderId());
            }

            // Load order products
            orderProducts = new ArrayList<>(selectedOrder != null ? selectedOrder.getOrderDetails() : new ArrayList<>());

            // Load existing details and create a map by product ID for quick lookup
            currentDetails = new ArrayList<>();
            java.util.Map<Long, PurchaseReceiptDetail> existingDetailsMap = new java.util.HashMap<>();

            for (PurchaseReceiptDetail existingDetail : currentReceipt.getReceiptDetails()) {
                if (existingDetail.getProduct() != null) {
                    existingDetailsMap.put(existingDetail.getProduct().getProductId(), existingDetail);
                }
            }

            // For each product in the order, either use existing detail or create new one
            for (PurchaseOrderDetail orderDetail : orderProducts) {
                Long productId = orderDetail.getProduct().getProductId();

                if (existingDetailsMap.containsKey(productId)) {
                    // Use existing detail
                    currentDetails.add(existingDetailsMap.get(productId));
                } else {
                    // Create new pre-filled detail for this product
                    PurchaseReceiptDetail newDetail = PurchaseReceiptDetail.builder()
                            .product(orderDetail.getProduct())
                            .purchaseOrderDetail(orderDetail)
                            .quantityReceived(0) // Initialize to 0, user will fill
                            .quantityDamaged(0)
                            .unitCost(orderDetail.getUnitCost())
                            .salePrice(orderDetail.getExpectedSalePrice() != null
                                    ? orderDetail.getExpectedSalePrice()
                                    : orderDetail.getUnitCost().multiply(new BigDecimal("1.5")))
                            .batchNumber("") // User will fill
                            .build();
                    currentDetails.add(newDetail);
                }
            }

            // Prepare new detail
            prepareNewDetail();

        } catch (Exception e) {
            showErrorMessage("Error al cargar la recepción: " + e.getMessage());
        }
    }

    /**
     * Navigate to form page for editing a receipt.
     */
    public String editReceipt(PurchaseReceipt receipt) {
        if (receipt == null) {
            showErrorMessage("Recepción no encontrada");
            return null;
        }
        return "purchase-receipt-form.xhtml?faces-redirect=true&receiptId=" + receipt.getReceiptId();
    }

    /**
     * Delete a receipt.
     */
    public void delete() {
        try {
            if (selectedReceipt == null) {
                showWarningMessage("Debe seleccionar una recepción");
                return;
            }

            receiptService.delete(selectedReceipt);
            showSuccessMessage("Recepción eliminada exitosamente");
            loadPurchaseReceipts();

        } catch (Exception e) {
            showErrorMessage("Error al eliminar la recepción: " + e.getMessage());
        }
    }

    /**
     * Calculate total quantities for display.
     */
    public Integer getTotalQuantityReceived() {
        if (currentDetails == null || currentDetails.isEmpty()) {
            return 0;
        }
        return currentDetails.stream()
                .mapToInt(d -> d.getQuantityReceived() != null ? d.getQuantityReceived() : 0)
                .sum();
    }

    public Integer getTotalQuantityDamaged() {
        if (currentDetails == null || currentDetails.isEmpty()) {
            return 0;
        }
        return currentDetails.stream()
                .mapToInt(d -> d.getQuantityDamaged() != null ? d.getQuantityDamaged() : 0)
                .sum();
    }

    public Integer getTotalQuantityAvailable() {
        return getTotalQuantityReceived() - getTotalQuantityDamaged();
    }

    /**
     * Check if current user can edit a receipt.
     */
    public boolean canEdit(PurchaseReceipt receipt) {
        return receiptService.canEdit(receipt);
    }

    /**
     * Check if current user can delete a receipt.
     */
    public boolean canDelete(PurchaseReceipt receipt) {
        return receiptService.canDelete(receipt);
    }

    /**
     * Check if current user can complete a receipt.
     */
    public boolean canComplete(PurchaseReceipt receipt) {
        return receiptService.canComplete(receipt);
    }

    /**
     * Check if an order can have a receipt started.
     */
    public boolean canStartReceipt(PurchaseOrder order) {
        return order != null && receiptService.canStartReceiptForOrder(order.getOrderId());
    }

    /**
     * Check if user is ADMIN or STOREKEEPER (required for this page).
     */
    public void checkAccess() {
        if (userController != null) {
            if (!userController.isAdmin() && !userController.isAdminOrStorekeeper()) {
                FacesContext.getCurrentInstance().getApplication()
                        .getNavigationHandler()
                        .handleNavigation(FacesContext.getCurrentInstance(), null,
                                "home.xhtml?faces-redirect=true");
            }
        }
    }

    /**
     * Navigate to form page for starting new receipt.
     */
    public String startNewReceipt(PurchaseOrder order) {
        prepareStartReceipt(order);
        return startReceipt();
    }

    /**
     * Return to purchase receipts list.
     */
    public String returnToList() {
        return "purchase-receipts.xhtml?faces-redirect=true";
    }

    /**
     * Close a PrimeFaces dialog.
     */
    private void closeDialog(String dialogWidgetVar) {
        FacesContext.getCurrentInstance()
                .getPartialViewContext()
                .getEvalScripts()
                .add("PF('" + dialogWidgetVar + "').hide();");
    }

    // Utility methods for messages
    private void showSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", message));
    }

    private void showInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Información", message));
    }

    private void showWarningMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", message));
    }

    private void showErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
    }
}
