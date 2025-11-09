package com.mycompany.model.entity.enums;

/**
 * Enumeration representing the various states of a purchase order in the system.
 *
 * Workflow:
 * DRAFT → SENT → CONFIRMED → IN_TRANSIT → RECEIVING → RECEIVED
 *    ↓
 * CANCELLED (can happen from DRAFT or SENT)
 *
 * @author ramir
 */
public enum PurchaseOrderStatus {

    /**
     * Order is being created, can be edited.
     * Initial state when admin creates a new purchase order.
     */
    DRAFT("Borrador"),

    /**
     * Order has been sent to supplier.
     * Waiting for supplier confirmation.
     */
    SENT("Enviada"),

    /**
     * Supplier has confirmed the order.
     * May include supplier's confirmation number.
     */
    CONFIRMED("Confirmada"),

    /**
     * Products are in transit from supplier to pharmacy.
     */
    IN_TRANSIT("En Tránsito"),

    /**
     * Products are being received at the pharmacy.
     * Storekeeper is processing the receipt.
     */
    RECEIVING("Recibiendo"),

    /**
     * All products have been received and processed.
     * Order is complete.
     */
    RECEIVED("Recibida"),

    /**
     * Order has been cancelled.
     * Includes cancellation reason and who cancelled it.
     */
    CANCELLED("Cancelada");

    private final String displayName;

    PurchaseOrderStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the user-friendly display name for this status.
     *
     * @return Spanish display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if this status allows editing the order.
     * Only DRAFT orders can be edited.
     *
     * @return true if order can be edited, false otherwise
     */
    public boolean isEditable() {
        return this == DRAFT;
    }

    /**
     * Check if this status allows cancellation.
     * Only DRAFT and SENT orders can be cancelled.
     *
     * @return true if order can be cancelled, false otherwise
     */
    public boolean isCancellable() {
        return this == DRAFT || this == SENT;
    }

    /**
     * Check if this is a final state (cannot transition to other states).
     *
     * @return true if status is final, false otherwise
     */
    public boolean isFinal() {
        return this == RECEIVED || this == CANCELLED;
    }

    /**
     * Get the appropriate PrimeFaces severity for displaying this status.
     * Used for badge colors in the UI.
     *
     * @return PrimeFaces severity string
     */
    public String getSeverity() {
        switch (this) {
            case DRAFT:
                return "info";
            case SENT:
                return "warning";
            case CONFIRMED:
            case IN_TRANSIT:
                return "primary";
            case RECEIVING:
                return "help";
            case RECEIVED:
                return "success";
            case CANCELLED:
                return "danger";
            default:
                return "secondary";
        }
    }
}
