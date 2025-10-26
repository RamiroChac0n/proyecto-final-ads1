package com.mycompany.service.impl;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.PurchaseOrder;
import com.mycompany.model.entity.PurchaseOrderDetail;
import com.mycompany.model.entity.Supplier;
import com.mycompany.model.entity.enums.PurchaseOrderStatus;
import com.mycompany.repository.PurchaseOrderRepository;
import com.mycompany.service.IPurchaseOrderService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Implementation of IPurchaseOrderService.
 * Handles business logic for purchase order operations.
 *
 * @author ramir
 */
@Stateless
public class PurchaseOrderServiceImpl implements IPurchaseOrderService {

    @EJB
    private PurchaseOrderRepository purchaseOrderRepository;

    @Override
    public PurchaseOrder save(PurchaseOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("La orden de compra no puede ser nula");
        }

        // Validate required fields
        if (order.getSupplier() == null) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor");
        }
        if (order.getBranch() == null) {
            throw new IllegalArgumentException("Debe seleccionar una sucursal");
        }

        // Generate order number if not set
        if (order.getOrderNumber() == null || order.getOrderNumber().trim().isEmpty()) {
            order.setOrderNumber(generateOrderNumber(order.getBranch()));
        }

        // Set initial status if not set
        if (order.getStatus() == null) {
            order.setStatus(PurchaseOrderStatus.DRAFT);
        }

        // Calculate totals
        order.calculateTotals();

        return purchaseOrderRepository.save(order);
    }

    @Override
    public PurchaseOrder edit(PurchaseOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("La orden de compra no puede ser nula");
        }

        // Verify order exists
        PurchaseOrder existing = purchaseOrderRepository.findById(order.getOrderId());
        if (existing == null) {
            throw new IllegalArgumentException("La orden de compra no existe");
        }

        // Check if order can be edited
        if (!canEdit(order)) {
            throw new IllegalStateException(
                "Solo se pueden editar órdenes en estado BORRADOR. Estado actual: " +
                order.getStatus().getDisplayName()
            );
        }

        // Recalculate totals
        order.calculateTotals();

        return purchaseOrderRepository.update(order);
    }

    @Override
    public void delete(PurchaseOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("La orden de compra no puede ser nula");
        }

        if (!canDelete(order)) {
            throw new IllegalStateException(
                "Solo se pueden eliminar órdenes en estado BORRADOR. Estado actual: " +
                order.getStatus().getDisplayName()
            );
        }

        purchaseOrderRepository.delete(order);
    }

    @Override
    public List<PurchaseOrder> list() {
        return purchaseOrderRepository.findAll();
    }

    @Override
    public PurchaseOrder findById(Integer orderId) {
        if (orderId == null) {
            return null;
        }
        return purchaseOrderRepository.findById(orderId);
    }

    @Override
    public PurchaseOrder findByIdWithDetails(Integer orderId) {
        if (orderId == null) {
            return null;
        }
        return purchaseOrderRepository.findByIdWithDetails(orderId);
    }

    @Override
    public PurchaseOrder findByOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            return null;
        }
        return purchaseOrderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public List<PurchaseOrder> findBySupplier(Supplier supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("El proveedor no puede ser nulo");
        }
        return purchaseOrderRepository.findBySupplier(supplier);
    }

    @Override
    public List<PurchaseOrder> findByBranch(Branch branch) {
        if (branch == null) {
            throw new IllegalArgumentException("La sucursal no puede ser nula");
        }
        return purchaseOrderRepository.findByBranch(branch);
    }

    @Override
    public List<PurchaseOrder> findByStatus(PurchaseOrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        return purchaseOrderRepository.findByStatus(status);
    }

    @Override
    public List<PurchaseOrder> findByDateRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("La fecha inicial debe ser anterior a la fecha final");
        }
        return purchaseOrderRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public List<PurchaseOrder> findPendingOrders() {
        return purchaseOrderRepository.findPendingOrders();
    }

    @Override
    public PurchaseOrder addDetail(PurchaseOrder order, PurchaseOrderDetail detail) {
        if (order == null) {
            throw new IllegalArgumentException("La orden de compra no puede ser nula");
        }
        if (detail == null) {
            throw new IllegalArgumentException("El detalle no puede ser nulo");
        }

        if (!canEdit(order)) {
            throw new IllegalStateException(
                "Solo se pueden agregar productos a órdenes en estado BORRADOR"
            );
        }

        // Validate detail
        if (detail.getProduct() == null) {
            throw new IllegalArgumentException("Debe seleccionar un producto");
        }
        if (detail.getQuantityOrdered() == null || detail.getQuantityOrdered() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (detail.getUnitCost() == null || detail.getUnitCost().signum() < 0) {
            throw new IllegalArgumentException("El costo unitario debe ser mayor o igual a 0");
        }

        // Add detail to order
        order.addDetail(detail);

        // Recalculate totals
        order.calculateTotals();

        return purchaseOrderRepository.update(order);
    }

    @Override
    public PurchaseOrder removeDetail(PurchaseOrder order, PurchaseOrderDetail detail) {
        if (order == null) {
            throw new IllegalArgumentException("La orden de compra no puede ser nula");
        }
        if (detail == null) {
            throw new IllegalArgumentException("El detalle no puede ser nulo");
        }

        if (!canEdit(order)) {
            throw new IllegalStateException(
                "Solo se pueden eliminar productos de órdenes en estado BORRADOR"
            );
        }

        // Remove detail from order
        order.removeDetail(detail);

        // Recalculate totals
        order.calculateTotals();

        return purchaseOrderRepository.update(order);
    }

    @Override
    public PurchaseOrder sendOrder(Integer orderId) {
        PurchaseOrder order = findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("La orden de compra no existe");
        }

        // Verify order is in DRAFT status
        if (order.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException(
                "Solo se pueden enviar órdenes en estado BORRADOR. Estado actual: " +
                order.getStatus().getDisplayName()
            );
        }

        // Verify order has details
        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            throw new IllegalArgumentException(
                "No se puede enviar una orden sin productos"
            );
        }

        // Update status and sent date
        order.setStatus(PurchaseOrderStatus.SENT);
        order.setSentDate(new Date());

        return purchaseOrderRepository.update(order);
    }

    @Override
    public PurchaseOrder confirmOrder(Integer orderId, String supplierConfirmationNumber) {
        PurchaseOrder order = findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("La orden de compra no existe");
        }

        // Verify order is in SENT status
        if (order.getStatus() != PurchaseOrderStatus.SENT) {
            throw new IllegalStateException(
                "Solo se pueden confirmar órdenes en estado ENVIADA. Estado actual: " +
                order.getStatus().getDisplayName()
            );
        }

        // Update status, confirmed date, and confirmation number
        order.setStatus(PurchaseOrderStatus.CONFIRMED);
        order.setConfirmedDate(new Date());
        if (supplierConfirmationNumber != null && !supplierConfirmationNumber.trim().isEmpty()) {
            order.setSupplierConfirmationNumber(supplierConfirmationNumber);
        }

        return purchaseOrderRepository.update(order);
    }

    @Override
    public PurchaseOrder cancelOrder(Integer orderId, String userId, String reason) {
        PurchaseOrder order = findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("La orden de compra no existe");
        }

        if (!canCancel(order)) {
            throw new IllegalStateException(
                "Solo se pueden cancelar órdenes en estado BORRADOR o ENVIADA. Estado actual: " +
                order.getStatus().getDisplayName()
            );
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar un motivo de cancelación");
        }

        // Update status and cancellation info
        order.setStatus(PurchaseOrderStatus.CANCELLED);
        order.setCancellationDate(new Date());
        order.setCancelledBy(userId);
        order.setCancellationReason(reason);

        return purchaseOrderRepository.update(order);
    }

    @Override
    public String generateOrderNumber(Branch branch) {
        if (branch == null) {
            throw new IllegalArgumentException("La sucursal no puede ser nula");
        }

        // Format: OC-{branchId}-{year}-{sequence}
        int year = LocalDate.now().getYear();
        String prefix = "OC-" + branch.getBranchId() + "-" + year + "-";

        // Find the highest sequence number for this branch and year
        List<PurchaseOrder> ordersThisYear = purchaseOrderRepository.findAll().stream()
            .filter(order -> order.getOrderNumber() != null &&
                           order.getOrderNumber().startsWith(prefix))
            .toList();

        int maxSequence = 0;
        for (PurchaseOrder order : ordersThisYear) {
            try {
                String[] parts = order.getOrderNumber().split("-");
                if (parts.length == 4) {
                    int sequence = Integer.parseInt(parts[3]);
                    if (sequence > maxSequence) {
                        maxSequence = sequence;
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore invalid order numbers
            }
        }

        int nextSequence = maxSequence + 1;
        return prefix + String.format("%06d", nextSequence);
    }

    @Override
    public boolean canEdit(PurchaseOrder order) {
        return order != null &&
               order.getStatus() != null &&
               order.getStatus().isEditable();
    }

    @Override
    public boolean canDelete(PurchaseOrder order) {
        return order != null &&
               order.getStatus() != null &&
               order.getStatus() == PurchaseOrderStatus.DRAFT;
    }

    @Override
    public boolean canCancel(PurchaseOrder order) {
        return order != null &&
               order.getStatus() != null &&
               order.getStatus().isCancellable();
    }

    @Override
    public Long countByStatus(PurchaseOrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        return purchaseOrderRepository.countByStatus(status);
    }
}
