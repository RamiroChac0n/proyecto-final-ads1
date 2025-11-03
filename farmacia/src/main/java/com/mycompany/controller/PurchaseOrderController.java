package com.mycompany.controller;

import com.mycompany.model.entity.*;
import com.mycompany.model.entity.enums.PurchaseOrderStatus;
import com.mycompany.repository.BranchRepository;
import com.mycompany.service.IPurchaseOrderService;
import com.mycompany.service.IProductService;
import com.mycompany.service.ISupplierService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JSF Managed Bean controller for purchase order management (ADMIN only).
 * <p>
 * This view-scoped controller manages the complete purchase order lifecycle from creation
 * to approval, confirmation, and eventual receipt. Purchase orders represent formal requests
 * to suppliers for products needed to restock inventory.
 * </p>
 *
 * <h3>Purchase Order Workflow:</h3>
 * <ol>
 *   <li><strong>DRAFT:</strong> Order is being created/edited
 *     <ul>
 *       <li>Select supplier and branch</li>
 *       <li>Add products with quantities and expected costs</li>
 *       <li>Calculate totals (subtotal, tax, shipping, total)</li>
 *     </ul>
 *   </li>
 *   <li><strong>PENDING:</strong> Submitted for approval (awaiting administrator review)</li>
 *   <li><strong>APPROVED:</strong> Approved by administrator, ready to send to supplier</li>
 *   <li><strong>CONFIRMED:</strong> Supplier confirmed order and provided confirmation number</li>
 *   <li><strong>COMPLETED:</strong> Products received via PurchaseReceipt</li>
 *   <li><strong>CANCELLED:</strong> Order cancelled with reason documented</li>
 * </ol>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Multi-Product Orders:</strong> Add multiple products to a single order</li>
 *   <li><strong>Cost Estimation:</strong> Enter expected unit costs and sale prices</li>
 *   <li><strong>Tax & Shipping:</strong> Calculate IVA (12%) and shipping costs</li>
 *   <li><strong>Status Tracking:</strong> Complete status workflow from draft to completion</li>
 *   <li><strong>Approval Process:</strong> Submit orders for admin approval</li>
 *   <li><strong>Supplier Confirmation:</strong> Record supplier confirmation numbers</li>
 *   <li><strong>Cancellation:</strong> Cancel orders with documented reasons</li>
 *   <li><strong>Receipt Integration:</strong> Link to PurchaseReceiptController for receiving goods</li>
 * </ul>
 *
 * <h3>Order Calculations:</h3>
 * <ul>
 *   <li><strong>Subtotal:</strong> Sum of (quantity × unit cost) for all order details</li>
 *   <li><strong>Tax (IVA):</strong> 12% of subtotal</li>
 *   <li><strong>Total:</strong> Subtotal + Tax + Shipping Cost</li>
 * </ul>
 *
 * <h3>Access Control:</h3>
 * <p>
 * Only ADMIN users can create, edit, approve, and manage purchase orders.
 * Enforced via {@link #checkAdminAccess()}.
 * </p>
 *
 * @author ramir
 * @version 1.0
 * @see PurchaseOrder
 * @see PurchaseOrderDetail
 * @see PurchaseOrderStatus
 * @see IPurchaseOrderService
 * @see PurchaseReceiptController
 */
@Data
@Named(value = "purchaseOrderController")
@ViewScoped
public class PurchaseOrderController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private IPurchaseOrderService purchaseOrderService;

    @EJB
    private ISupplierService supplierService;

    @EJB
    private IProductService productService;

    @EJB
    private BranchRepository branchRepository;

    @Inject
    private UserController userController;

    // Current purchase order being created/edited
    private PurchaseOrder currentOrder;
    private List<PurchaseOrder> purchaseOrders;

    // Order details management
    private List<PurchaseOrderDetail> currentDetails;
    private PurchaseOrderDetail newDetail;

    // Dropdowns and selections
    private List<Supplier> suppliers;
    private List<Branch> branches;
    private List<Product> products;
    private Product selectedProduct;

    // Filters
    private PurchaseOrderStatus filterStatus;
    private List<PurchaseOrderStatus> statusList;

    // Selected order for viewing/actions
    private PurchaseOrder selectedOrder;

    // Cancellation
    private String cancellationReason;

    // Confirmation
    private String supplierConfirmationNumber;

    @PostConstruct
    public void init() {
        checkAdminAccess();
        loadPurchaseOrders();
        loadSuppliers();
        loadBranches();
        loadProducts();
        initializeStatusList();

        // CRITICAL: Initialize currentOrder early to avoid NPE during XHTML rendering
        // Check for orderId parameter and load existing order, otherwise create new
        FacesContext context = FacesContext.getCurrentInstance();
        String orderIdParam = context.getExternalContext().getRequestParameterMap().get("orderId");

        if (orderIdParam != null && !orderIdParam.isEmpty()) {
            try {
                Integer orderId = Integer.parseInt(orderIdParam);
                PurchaseOrder order = loadOrderById(orderId);
                if (order != null) {
                    edit(order);
                } else {
                    createNew();
                }
            } catch (NumberFormatException e) {
                showErrorMessage("ID de orden inválido");
                createNew();
            }
        } else {
            // No orderId parameter - initialize with new order to prevent NPE
            createNew();
        }

        // Initialize basic structures to avoid NPE during rendering
        if (currentDetails == null) {
            currentDetails = new ArrayList<>();
        }
        if (newDetail == null) {
            prepareNewDetail();
        }
    }

    /**
     * Initialize form - called from purchase-order-form.xhtml preRenderView.
     * Now redundant as initialization happens in @PostConstruct, but kept for compatibility.
     */
    public void initForm() {
        // Initialization now happens in @PostConstruct
        // This method is kept for backward compatibility with XHTML preRenderView
        // and to ensure currentOrder is never null
        if (currentOrder == null) {
            createNew();
        }
    }

    /**
     * Load order by ID without opening dialog.
     */
    private PurchaseOrder loadOrderById(Integer orderId) {
        try {
            PurchaseOrder order = purchaseOrderService.findByIdWithDetails(orderId);
            if (order == null) {
                showErrorMessage("Orden no encontrada");
                return null;
            }
            return order;
        } catch (Exception e) {
            showErrorMessage("Error al cargar la orden: " + e.getMessage());
            return null;
        }
    }

    /**
     * Load all purchase orders from database.
     */
    private void loadPurchaseOrders() {
        try {
            purchaseOrders = purchaseOrderService.list();
        } catch (Exception e) {
            showErrorMessage("Error al cargar órdenes de compra: " + e.getMessage());
        }
    }

    /**
     * Load all active suppliers.
     */
    private void loadSuppliers() {
        try {
            suppliers = supplierService.list().stream()
                    .filter(Supplier::getIsActive)
                    .toList();
        } catch (Exception e) {
            showErrorMessage("Error al cargar proveedores: " + e.getMessage());
        }
    }

    /**
     * Load all active branches.
     */
    private void loadBranches() {
        try {
            branches = branchRepository.findAll().stream()
                    .filter(Branch::getIsActive)
                    .toList();
        } catch (Exception e) {
            showErrorMessage("Error al cargar sucursales: " + e.getMessage());
        }
    }

    /**
     * Load all active products.
     */
    private void loadProducts() {
        try {
            products = productService.list().stream()
                    .filter(Product::getIsActive)
                    .toList();
        } catch (Exception e) {
            showErrorMessage("Error al cargar productos: " + e.getMessage());
        }
    }

    /**
     * Initialize status list for filter dropdown.
     */
    private void initializeStatusList() {
        statusList = Arrays.asList(PurchaseOrderStatus.values());
    }

    /**
     * Create a new purchase order.
     * Opens dialog for creating order.
     */
    public void createNew() {
        currentOrder = PurchaseOrder.builder()
                .status(PurchaseOrderStatus.DRAFT)
                .shippingCost(BigDecimal.ZERO)
                .createdBy(userController.getUser().getId())
                .build();
        currentDetails = new ArrayList<>();
        prepareNewDetail();
    }

    /**
     * Prepare a new detail for adding to order.
     */
    private void prepareNewDetail() {
        newDetail = PurchaseOrderDetail.builder()
                .quantityOrdered(1)
                .unitCost(BigDecimal.ZERO)
                .expectedSalePrice(BigDecimal.ZERO)
                .build();
        selectedProduct = null;
    }

    /**
     * Add current product to order details.
     */
    public void addProductToOrder() {
        try {
            if (selectedProduct == null) {
                showWarningMessage("Debe seleccionar un producto");
                return;
            }

            if (newDetail.getQuantityOrdered() == null || newDetail.getQuantityOrdered() <= 0) {
                showWarningMessage("La cantidad debe ser mayor a 0");
                return;
            }

            if (newDetail.getUnitCost() == null || newDetail.getUnitCost().compareTo(BigDecimal.ZERO) < 0) {
                showWarningMessage("El costo unitario debe ser mayor o igual a 0");
                return;
            }

            // Check if product already in details
            boolean alreadyExists = currentDetails.stream()
                    .anyMatch(d -> d.getProduct() != null &&
                                 d.getProduct().getProductId().equals(selectedProduct.getProductId()));

            if (alreadyExists) {
                showWarningMessage("El producto ya está en la orden. Puede modificar la cantidad directamente.");
                return;
            }

            // Set product and calculate subtotal
            newDetail.setProduct(selectedProduct);
            newDetail.calculateSubtotal();

            // Add to current details
            currentDetails.add(newDetail);

            // Prepare for next product
            prepareNewDetail();

            showInfoMessage("Producto agregado correctamente");
        } catch (Exception e) {
            showErrorMessage("Error al agregar producto: " + e.getMessage());
        }
    }

    /**
     * Remove a product from order details.
     */
    public void removeProductFromOrder(PurchaseOrderDetail detail) {
        currentDetails.remove(detail);
        showInfoMessage("Producto eliminado");
    }

    /**
     * Recalculate subtotal for a detail when quantity or cost changes.
     */
    public void recalculateDetailSubtotal(PurchaseOrderDetail detail) {
        if (detail != null) {
            detail.calculateSubtotal();
        }
    }

    /**
     * Calculate order totals from current details.
     */
    public BigDecimal calculateSubtotal() {
        if (currentDetails == null || currentDetails.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return currentDetails.stream()
                .map(PurchaseOrderDetail::getSubtotal)
                .filter(s -> s != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTax() {
        return calculateSubtotal().multiply(new BigDecimal("0.12"));
    }

    public BigDecimal calculateTotal() {
        BigDecimal total = calculateSubtotal().add(calculateTax());
        if (currentOrder != null && currentOrder.getShippingCost() != null) {
            total = total.add(currentOrder.getShippingCost());
        }
        return total;
    }

    /**
     * Save the current purchase order.
     */
    public String save() {
        try {
            // Validate
            if (currentOrder.getSupplier() == null) {
                showWarningMessage("Debe seleccionar un proveedor");
                return null;
            }

            if (currentOrder.getBranch() == null) {
                showWarningMessage("Debe seleccionar una sucursal");
                return null;
            }

            if (currentDetails.isEmpty()) {
                showWarningMessage("Debe agregar al menos un producto");
                return null;
            }

            // Set order details
            currentOrder.setOrderDetails(new ArrayList<>());
            for (PurchaseOrderDetail detail : currentDetails) {
                currentOrder.addDetail(detail);
            }

            // Calculate totals
            currentOrder.calculateTotals();

            // Save
            if (currentOrder.getOrderId() == null) {
                purchaseOrderService.save(currentOrder);
                showSuccessMessage("Orden de compra creada exitosamente: " + currentOrder.getOrderNumber());
            } else {
                purchaseOrderService.edit(currentOrder);
                showSuccessMessage("Orden de compra actualizada exitosamente");
            }

            // Redirect to list
            return returnToList();

        } catch (Exception e) {
            showErrorMessage("Error al guardar la orden: " + e.getMessage());
            return null;
        }
    }

    /**
     * Edit an existing purchase order.
     */
    public void edit(PurchaseOrder order) {
        if (order == null) {
            return;
        }

        try {
            if (!purchaseOrderService.canEdit(order)) {
                showWarningMessage("Solo se pueden editar órdenes en estado BORRADOR");
                return;
            }

            currentOrder = order;

            // Load current details
            currentDetails = new ArrayList<>(currentOrder.getOrderDetails());

            // Prepare new detail
            prepareNewDetail();

        } catch (Exception e) {
            showErrorMessage("Error al cargar la orden: " + e.getMessage());
        }
    }

    /**
     * Navigate to form page for editing an order.
     */
    public String editOrder(PurchaseOrder order) {
        if (order == null) {
            showErrorMessage("Orden no encontrada");
            return null;
        }
        return "purchase-order-form.xhtml?faces-redirect=true&orderId=" + order.getOrderId();
    }

    /**
     * Delete a purchase order.
     */
    public void delete() {
        try {
            if (selectedOrder == null) {
                showWarningMessage("Debe seleccionar una orden");
                return;
            }

            purchaseOrderService.delete(selectedOrder);
            showSuccessMessage("Orden eliminada exitosamente");
            loadPurchaseOrders();

        } catch (Exception e) {
            showErrorMessage("Error al eliminar la orden: " + e.getMessage());
        }
    }

    /**
     * Send order to supplier.
     */
    public void sendOrder(PurchaseOrder order) {
        try {
            purchaseOrderService.sendOrder(order.getOrderId());
            showSuccessMessage("Orden enviada al proveedor");
            loadPurchaseOrders();
        } catch (Exception e) {
            showErrorMessage("Error al enviar la orden: " + e.getMessage());
        }
    }

    /**
     * Confirm order.
     */
    public void confirmOrder() {
        try {
            if (selectedOrder == null) {
                showWarningMessage("Debe seleccionar una orden");
                return;
            }

            purchaseOrderService.confirmOrder(
                selectedOrder.getOrderId(),
                supplierConfirmationNumber
            );
            showSuccessMessage("Orden confirmada exitosamente");
            loadPurchaseOrders();
            closeDialog("confirmOrderDialogVar");

        } catch (Exception e) {
            showErrorMessage("Error al confirmar la orden: " + e.getMessage());
        }
    }

    /**
     * Cancel a purchase order.
     */
    public void cancelOrder() {
        try {
            if (selectedOrder == null) {
                showWarningMessage("Debe seleccionar una orden");
                return;
            }

            if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
                showWarningMessage("Debe proporcionar un motivo de cancelación");
                return;
            }

            purchaseOrderService.cancelOrder(
                selectedOrder.getOrderId(),
                userController.getUser().getId(),
                cancellationReason
            );

            showSuccessMessage("Orden cancelada exitosamente");
            loadPurchaseOrders();
            closeDialog("cancelDialog");

        } catch (Exception e) {
            showErrorMessage("Error al cancelar la orden: " + e.getMessage());
        }
    }

    /**
     * Prepare confirmation dialog.
     */
    public void prepareConfirm(PurchaseOrder order) {
        selectedOrder = order;
        supplierConfirmationNumber = null;
    }

    /**
     * Prepare cancellation dialog.
     */
    public void prepareCancel(PurchaseOrder order) {
        selectedOrder = order;
        cancellationReason = null;
    }

    /**
     * Check if current user can edit an order.
     */
    public boolean canEdit(PurchaseOrder order) {
        return purchaseOrderService.canEdit(order);
    }

    /**
     * Check if current user can delete an order.
     */
    public boolean canDelete(PurchaseOrder order) {
        return purchaseOrderService.canDelete(order);
    }

    /**
     * Check if current user can cancel an order.
     */
    public boolean canCancel(PurchaseOrder order) {
        return purchaseOrderService.canCancel(order);
    }

    /**
     * Check if current user can send an order.
     */
    public boolean canSend(PurchaseOrder order) {
        return order != null && order.getStatus() == PurchaseOrderStatus.DRAFT;
    }

    /**
     * Check if current user can confirm an order.
     */
    public boolean canConfirm(PurchaseOrder order) {
        return order != null && order.getStatus() == PurchaseOrderStatus.SENT;
    }

    /**
     * Check if user is admin (required for this page).
     */
    public void checkAdminAccess() {
        if (userController != null) {
            userController.checkAdminAccess();
        }
    }

    /**
     * Navigate to form page for creating new order.
     */
    public String createNewOrder() {
        return "purchase-order-form.xhtml?faces-redirect=true";
    }

    /**
     * Return to purchase orders list.
     */
    public String returnToList() {
        return "purchase-orders.xhtml?faces-redirect=true";
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
