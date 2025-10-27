package com.mycompany.service.impl;

import com.mycompany.model.entity.*;
import com.mycompany.model.entity.enums.MovementType;
import com.mycompany.model.entity.enums.PurchaseOrderStatus;
import com.mycompany.model.entity.enums.PurchaseReceiptStatus;
import com.mycompany.repository.*;
import com.mycompany.service.IPurchaseOrderService;
import com.mycompany.service.IPurchaseReceiptService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Service implementation for Purchase Receipt business logic.
 * Handles product reception, validation, and batch creation.
 *
 * @author ramir
 */
@Stateless
public class PurchaseReceiptServiceImpl implements IPurchaseReceiptService {

    @EJB
    private PurchaseReceiptRepository receiptRepository;

    @EJB
    private PurchaseReceiptDetailRepository detailRepository;

    @EJB
    private PurchaseOrderRepository orderRepository;

    @EJB
    private ProductBatchRepository batchRepository;

    @EJB
    private InventoryMovementRepository movementRepository;

    @EJB
    private IPurchaseOrderService orderService;

    @Override
    public PurchaseReceipt startReceipt(Integer orderId, String receivedBy, Integer branchId,
                                         String supplierInvoiceNumber, LocalDate supplierInvoiceDate) {
        // Validate order exists and is in receivable status
        PurchaseOrder order = orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Orden de compra no encontrada");
        }

        if (!canStartReceiptForOrder(orderId)) {
            throw new IllegalStateException(
                "La orden debe estar en estado CONFIRMED, IN_TRANSIT o RECEIVING para iniciar recepción. " +
                "Estado actual: " + order.getStatus().getDisplayName()
            );
        }

        // Check for existing completed receipts to determine if partial
        List<PurchaseReceipt> existingReceipts = receiptRepository.findByOrderId(orderId);
        long completedReceipts = existingReceipts.stream()
                .filter(r -> r.getStatus() == PurchaseReceiptStatus.COMPLETE)
                .count();
        boolean isPartial = completedReceipts > 0;

        // Generate receipt number
        String receiptNumber = generateReceiptNumber(orderId);

        // Create new receipt
        PurchaseReceipt receipt = PurchaseReceipt.builder()
                .purchaseOrder(order)
                .receiptNumber(receiptNumber)
                .receivedBy(receivedBy)
                .branch(order.getBranch()) // Use order's branch if not specified
                .supplierInvoiceNumber(supplierInvoiceNumber)
                .supplierInvoiceDate(supplierInvoiceDate)
                .isPartial(isPartial)
                .isFinal(false)
                .status(PurchaseReceiptStatus.DRAFT)
                .build();

        // Save receipt
        PurchaseReceipt savedReceipt = receiptRepository.save(receipt);

        // Update order status to RECEIVING if not already
        if (order.getStatus() != PurchaseOrderStatus.RECEIVING) {
            order.setStatus(PurchaseOrderStatus.RECEIVING);
            order.setReceivingStartedAt(new Date());
            orderRepository.update(order);
        }

        return savedReceipt;
    }

    @Override
    public PurchaseReceipt save(PurchaseReceipt receipt) {
        if (!canEdit(receipt)) {
            throw new IllegalStateException("Solo se pueden guardar recepciones en estado BORRADOR");
        }
        return receiptRepository.save(receipt);
    }

    @Override
    public PurchaseReceipt edit(PurchaseReceipt receipt) {
        if (!canEdit(receipt)) {
            throw new IllegalStateException("Solo se pueden editar recepciones en estado BORRADOR");
        }
        return receiptRepository.update(receipt);
    }

    @Override
    public void delete(PurchaseReceipt receipt) {
        if (!canDelete(receipt)) {
            throw new IllegalStateException("Solo se pueden eliminar recepciones en estado BORRADOR");
        }
        receiptRepository.delete(receipt);
    }

    @Override
    public List<PurchaseReceipt> list() {
        return receiptRepository.findAll();
    }

    @Override
    public PurchaseReceipt findById(Integer receiptId) {
        return receiptRepository.findById(receiptId);
    }

    @Override
    public PurchaseReceipt findByIdWithDetails(Integer receiptId) {
        return receiptRepository.findByIdWithDetails(receiptId);
    }

    @Override
    public List<PurchaseReceipt> findByOrderId(Integer orderId) {
        return receiptRepository.findByOrderId(orderId);
    }

    @Override
    public List<PurchaseReceipt> findByStatus(PurchaseReceiptStatus status) {
        return receiptRepository.findByStatus(status);
    }

    @Override
    public List<PurchaseReceipt> findByDateRange(Date startDate, Date endDate) {
        return receiptRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public List<PurchaseReceipt> findDraftReceipts() {
        return receiptRepository.findDraftReceipts();
    }

    @Override
    public List<PurchaseReceipt> findCompletedReceipts() {
        return receiptRepository.findCompletedReceipts();
    }

    @Override
    public PurchaseReceipt addDetail(PurchaseReceipt receipt, PurchaseReceiptDetail detail) {
        if (!canEdit(receipt)) {
            throw new IllegalStateException("No se pueden agregar productos a una recepción completada");
        }

        receipt.addDetail(detail);
        return receiptRepository.update(receipt);
    }

    @Override
    public PurchaseReceipt removeDetail(PurchaseReceipt receipt, PurchaseReceiptDetail detail) {
        if (!canEdit(receipt)) {
            throw new IllegalStateException("No se pueden remover productos de una recepción completada");
        }

        receipt.removeDetail(detail);
        return receiptRepository.update(receipt);
    }

    @Override
    public PurchaseReceipt completeReceipt(Integer receiptId, String completedBy,
                                            Boolean isFinal, String notes) {
        // Load receipt with details
        PurchaseReceipt receipt = findByIdWithDetails(receiptId);
        if (receipt == null) {
            throw new IllegalArgumentException("Recepción no encontrada");
        }

        // Validate receipt
        if (!validateReceipt(receipt)) {
            throw new IllegalStateException("La recepción no es válida para completar");
        }

        if (!canComplete(receipt)) {
            throw new IllegalStateException("La recepción no puede ser completada");
        }

        // Get associated order
        PurchaseOrder order = receipt.getPurchaseOrder();

        // Calculate if this should be marked as final
        boolean shouldBeFinal = determineFinalStatus(receipt, isFinal);

        // Process each detail: create batches and inventory movements
        for (PurchaseReceiptDetail detail : receipt.getReceiptDetails()) {
            processReceiptDetail(receipt, detail, order);
        }

        // Update receipt status
        receipt.setStatus(PurchaseReceiptStatus.COMPLETE);
        receipt.setCompletedDate(new Date());
        receipt.setCompletedBy(completedBy);
        receipt.setIsFinal(shouldBeFinal);

        // Add completion note
        String completionNote = String.format(
            "Completado el %s: %d unidades recibidas (%d dañadas). %s",
            new Date(),
            receipt.getTotalQuantityReceived(),
            receipt.getTotalQuantityDamaged(),
            shouldBeFinal ? "RECEPCIÓN FINAL" : "RECEPCIÓN PARCIAL"
        );
        if (notes != null && !notes.trim().isEmpty()) {
            completionNote += " - " + notes;
        }
        receipt.setNotes(completionNote);

        // Save updated receipt
        PurchaseReceipt completedReceipt = receiptRepository.update(receipt);

        // Update order status
        if (shouldBeFinal) {
            order.setStatus(PurchaseOrderStatus.RECEIVED);
            order.setReceivedDate(new Date());
        } else {
            // Keep in RECEIVING status for partial receipts
            order.setStatus(PurchaseOrderStatus.RECEIVING);
        }
        orderRepository.update(order);

        return completedReceipt;
    }

    /**
     * Process a single receipt detail: create batch and inventory movements.
     */
    private void processReceiptDetail(PurchaseReceipt receipt, PurchaseReceiptDetail detail,
                                       PurchaseOrder order) {
        // Calculate available quantity (received - damaged)
        Integer quantityAvailable = detail.getQuantityAvailable();

        // Create product batch
        ProductBatch batch = ProductBatch.builder()
                .product(detail.getProduct())
                .batchNumber(detail.getBatchNumber())
                .quantityReceived(detail.getQuantityReceived())
                .quantityAvailable(quantityAvailable)
                .unitCost(detail.getUnitCost())
                .salePrice(detail.getSalePrice())
                .manufactureDate(detail.getManufactureDate() != null ?
                        java.sql.Date.valueOf(detail.getManufactureDate()) : null)
                .expirationDate(java.sql.Date.valueOf(detail.getExpirationDate()))
                .receivedDate(receipt.getReceiptDate())
                .supplier(order.getSupplier())
                .purchaseOrder(order)
                .purchaseReceipt(receipt)
                .isActive(true)
                .build();

        ProductBatch savedBatch = batchRepository.save(batch);

        // Create User reference (JPA will handle the relationship without loading full entity)
        User userRef = User.builder().id(receipt.getReceivedBy()).build();

        // Create inventory movement for good products (IN)
        if (quantityAvailable > 0) {
            InventoryMovement inMovement = InventoryMovement.builder()
                    .product(detail.getProduct())
                    .batch(savedBatch)
                    .branch(receipt.getBranch())
                    .movementType(MovementType.IN)
                    .quantity(quantityAvailable)
                    .reason(String.format("Compra - OC: %s - Recepción: %s",
                            order.getOrderNumber(), receipt.getReceiptNumber()))
                    .user(userRef)
                    .build();

            movementRepository.save(inMovement);
        }

        // Create inventory movement for damaged products (OUT/ADJUSTMENT)
        if (detail.getQuantityDamaged() != null && detail.getQuantityDamaged() > 0) {
            InventoryMovement damageMovement = InventoryMovement.builder()
                    .product(detail.getProduct())
                    .batch(savedBatch)
                    .branch(receipt.getBranch())
                    .movementType(MovementType.ADJUSTMENT) // Using ADJUSTMENT for damages
                    .quantity(-detail.getQuantityDamaged()) // Negative for damage
                    .reason(String.format("Productos dañados en recepción - OC: %s - Recepción: %s",
                            order.getOrderNumber(), receipt.getReceiptNumber()))
                    .user(userRef)
                    .build();

            movementRepository.save(damageMovement);
        }
    }

    /**
     * Determine if this should be the final receipt for the order.
     */
    private boolean determineFinalStatus(PurchaseReceipt receipt, Boolean userSpecified) {
        // If user explicitly specified, use that
        if (userSpecified != null) {
            return userSpecified;
        }

        // Calculate total ordered vs total received
        PurchaseOrder order = receipt.getPurchaseOrder();

        // Sum all ordered quantities
        int totalOrdered = order.getOrderDetails().stream()
                .mapToInt(PurchaseOrderDetail::getQuantityOrdered)
                .sum();

        // Sum all received quantities from all completed receipts + current receipt
        List<PurchaseReceipt> allReceipts = receiptRepository.findByOrderId(order.getOrderId());
        int totalReceived = allReceipts.stream()
                .filter(r -> r.getStatus() == PurchaseReceiptStatus.COMPLETE ||
                             r.getReceiptId().equals(receipt.getReceiptId()))
                .flatMap(r -> r.getReceiptDetails().stream())
                .mapToInt(PurchaseReceiptDetail::getQuantityReceived)
                .sum();

        // Add current receipt's quantities
        totalReceived += receipt.getReceiptDetails().stream()
                .mapToInt(PurchaseReceiptDetail::getQuantityReceived)
                .sum();

        // If we've received all or more than ordered, it's final
        return totalReceived >= totalOrdered;
    }

    @Override
    public boolean validateReceipt(PurchaseReceipt receipt) {
        if (receipt == null) {
            return false;
        }

        // Must be in DRAFT status
        if (receipt.getStatus() != PurchaseReceiptStatus.DRAFT) {
            return false;
        }

        // Must have at least one detail
        if (receipt.getReceiptDetails() == null || receipt.getReceiptDetails().isEmpty()) {
            return false;
        }

        // Validate each detail
        for (PurchaseReceiptDetail detail : receipt.getReceiptDetails()) {
            // Must have product
            if (detail.getProduct() == null) {
                return false;
            }

            // Must have batch number
            if (detail.getBatchNumber() == null || detail.getBatchNumber().trim().isEmpty()) {
                return false;
            }

            // Must have valid quantities
            if (detail.getQuantityReceived() == null || detail.getQuantityReceived() < 0) {
                return false;
            }

            if (detail.getQuantityDamaged() != null &&
                detail.getQuantityDamaged() > detail.getQuantityReceived()) {
                return false;
            }

            // Must have expiration date
            if (detail.getExpirationDate() == null) {
                return false;
            }

            // Must have valid prices
            if (detail.getUnitCost() == null || detail.getSalePrice() == null) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean canEdit(PurchaseReceipt receipt) {
        return receipt != null && receipt.getStatus() == PurchaseReceiptStatus.DRAFT;
    }

    @Override
    public boolean canDelete(PurchaseReceipt receipt) {
        return receipt != null && receipt.getStatus() == PurchaseReceiptStatus.DRAFT;
    }

    @Override
    public boolean canComplete(PurchaseReceipt receipt) {
        return receipt != null &&
               receipt.getStatus() == PurchaseReceiptStatus.DRAFT &&
               validateReceipt(receipt);
    }

    @Override
    public boolean canStartReceiptForOrder(Integer orderId) {
        PurchaseOrder order = orderRepository.findById(orderId);
        if (order == null) {
            return false;
        }

        PurchaseOrderStatus status = order.getStatus();
        return status == PurchaseOrderStatus.CONFIRMED ||
               status == PurchaseOrderStatus.IN_TRANSIT ||
               status == PurchaseOrderStatus.RECEIVING;
    }

    @Override
    public String generateReceiptNumber(Integer orderId) {
        // Count existing complete receipts for this order
        List<PurchaseReceipt> existingReceipts = receiptRepository.findByOrderId(orderId);
        long completedCount = existingReceipts.stream()
                .filter(r -> r.getStatus() == PurchaseReceiptStatus.COMPLETE)
                .count();

        // Get current year
        int year = java.time.Year.now().getValue();

        // Find max sequence number for this year
        String prefix = "REC-" + year + "-";
        List<PurchaseReceipt> allReceipts = receiptRepository.findAll();

        int maxSequence = allReceipts.stream()
                .filter(r -> r.getReceiptNumber() != null &&
                            r.getReceiptNumber().startsWith(prefix))
                .mapToInt(r -> {
                    try {
                        String numPart = r.getReceiptNumber()
                                .substring(prefix.length())
                                .split("-")[0]; // Get part before -P if exists
                        return Integer.parseInt(numPart);
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);

        int nextSequence = maxSequence + 1;
        String baseNumber = prefix + String.format("%06d", nextSequence);

        // If this is a partial receipt (not the first), add -P{number}
        if (completedCount > 0) {
            return baseNumber + "-P" + (completedCount + 1);
        }

        return baseNumber;
    }

    @Override
    public Long countByStatus(PurchaseReceiptStatus status) {
        return receiptRepository.countByStatus(status);
    }
}
