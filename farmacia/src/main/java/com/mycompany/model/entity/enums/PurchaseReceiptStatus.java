package com.mycompany.model.entity.enums;

/**
 * Enumeration representing the various states of a purchase receipt in the system.
 *
 * Workflow:
 * DRAFT → COMPLETE
 *
 * @author ramir
 */
public enum PurchaseReceiptStatus {

    /**
     * Receipt is being created, products are being registered.
     * Initial state when storekeeper starts receiving products.
     */
    DRAFT("Borrador"),

    /**
     * Receipt has been completed and processed.
     * Product batches have been created and inventory has been updated.
     */
    COMPLETE("Completada");

    private final String displayName;

    PurchaseReceiptStatus(String displayName) {
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
     * Check if this status allows editing the receipt.
     * Only DRAFT receipts can be edited.
     *
     * @return true if receipt can be edited, false otherwise
     */
    public boolean isEditable() {
        return this == DRAFT;
    }

    /**
     * Check if this is a final state (cannot transition to other states).
     *
     * @return true if status is final, false otherwise
     */
    public boolean isFinal() {
        return this == COMPLETE;
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
                return "warning";
            case COMPLETE:
                return "success";
            default:
                return "secondary";
        }
    }
}
