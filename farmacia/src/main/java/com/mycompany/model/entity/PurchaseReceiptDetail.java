package com.mycompany.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * Entity representing a line item in a Purchase Receipt.
 * Maps to the 'purchase_receipt_details' table in the database.
 *
 * Each detail represents one product received with batch information,
 * quantities (including damaged items), and pricing.
 *
 * @author ramir
 */
@Entity
@Table(name = "purchase_receipt_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReceiptDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_detail_id")
    private Integer receiptDetailId;

    /**
     * Reference to the parent purchase receipt.
     * Required - detail cannot exist without a receipt.
     * When receipt is deleted, details are also deleted (CASCADE).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    @NotNull(message = "La recepción es requerida")
    private PurchaseReceipt purchaseReceipt;

    /**
     * Reference to the original purchase order detail.
     * Optional - links to what was originally ordered for comparison.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_detail_id")
    private PurchaseOrderDetail purchaseOrderDetail;

    /**
     * Product being received.
     * EAGER fetch to have product info available for display.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Debe seleccionar un producto")
    private Product product;

    /**
     * Batch number for this product lot.
     * Required - must track which batch products belong to.
     */
    @Column(name = "batch_number", nullable = false, length = 50)
    @NotBlank(message = "El número de lote es requerido")
    @Size(max = 50, message = "El número de lote no puede exceder 50 caracteres")
    private String batchNumber;

    /**
     * Quantity of this product received (including damaged items).
     * Must be >= 0.
     */
    @Column(name = "quantity_received", nullable = false)
    @NotNull(message = "La cantidad recibida es requerida")
    @Min(value = 0, message = "La cantidad recibida debe ser mayor o igual a 0")
    private Integer quantityReceived;

    /**
     * Quantity of damaged/defective products.
     * Must be >= 0 and <= quantityReceived.
     * Default: 0
     */
    @Column(name = "quantity_damaged")
    @Builder.Default
    @Min(value = 0, message = "La cantidad dañada debe ser mayor o igual a 0")
    private Integer quantityDamaged = 0;

    /**
     * Unit cost of the product (what we paid the supplier per unit).
     * Must be >= 0.00
     */
    @Column(name = "unit_cost", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El costo unitario es requerido")
    @DecimalMin(value = "0.00", message = "El costo unitario debe ser mayor o igual a 0")
    private BigDecimal unitCost;

    /**
     * Sale price per unit when this product is sold to customers.
     * Must be >= 0.00
     */
    @Column(name = "sale_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El precio de venta es requerido")
    @DecimalMin(value = "0.00", message = "El precio de venta debe ser mayor o igual a 0")
    private BigDecimal salePrice;

    /**
     * Date when this batch was manufactured.
     * Optional.
     */
    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    /**
     * Date when this batch expires.
     * Required - critical for pharmacy inventory management.
     */
    @Column(name = "expiration_date", nullable = false)
    @NotNull(message = "La fecha de vencimiento es requerida")
    private LocalDate expirationDate;

    /**
     * Additional notes for this product line.
     * Can include observations about packaging, condition, etc.
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    @Size(max = 5000, message = "Las notas no pueden exceder 5000 caracteres")
    private String notes;

    /**
     * Timestamp when this detail was created.
     * Auto-set on insert.
     */
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    /**
     * Lifecycle callback - executed before persisting new entity.
     * Sets creation timestamp.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();

        // Ensure damaged quantity is not null
        if (quantityDamaged == null) {
            quantityDamaged = 0;
        }

        // Validate damaged <= received
        if (quantityDamaged > quantityReceived) {
            throw new IllegalStateException("La cantidad dañada no puede ser mayor a la cantidad recibida");
        }
    }

    /**
     * Lifecycle callback - executed before updating entity.
     * Validates damaged quantity constraint.
     */
    @PreUpdate
    protected void onUpdate() {
        // Validate damaged <= received
        if (quantityDamaged != null && quantityReceived != null && quantityDamaged > quantityReceived) {
            throw new IllegalStateException("La cantidad dañada no puede ser mayor a la cantidad recibida");
        }
    }

    /**
     * Calculate the available quantity (received - damaged).
     * This is what actually goes into inventory.
     *
     * @return quantity available for sale
     */
    public Integer getQuantityAvailable() {
        if (quantityReceived == null) {
            return 0;
        }
        int damaged = (quantityDamaged != null) ? quantityDamaged : 0;
        return quantityReceived - damaged;
    }

    /**
     * Calculate the total cost for this line (quantity * unitCost).
     *
     * @return total cost for received products
     */
    public BigDecimal getTotalCost() {
        if (quantityReceived == null || unitCost == null) {
            return BigDecimal.ZERO;
        }
        return unitCost.multiply(new BigDecimal(quantityReceived));
    }

    /**
     * Calculate the profit margin for this product.
     * margin = (salePrice - unitCost) / unitCost
     *
     * @return profit margin as decimal (e.g., 0.25 for 25%), or null if not calculable
     */
    public BigDecimal getProfitMargin() {
        if (salePrice == null || unitCost == null ||
            unitCost.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal profit = salePrice.subtract(unitCost);
        return profit.divide(unitCost, 4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Check if quantity received matches quantity ordered.
     * Only valid if linked to an order detail.
     *
     * @return true if quantities match, false otherwise or if no order detail
     */
    public boolean matchesOrderedQuantity() {
        if (purchaseOrderDetail == null || quantityReceived == null) {
            return false;
        }
        return quantityReceived.equals(purchaseOrderDetail.getQuantityOrdered());
    }

    /**
     * Get the discrepancy between ordered and received quantities.
     * Positive = received more, Negative = received less, 0 = exact match
     *
     * @return quantity difference, or null if no order detail
     */
    public Integer getQuantityDiscrepancy() {
        if (purchaseOrderDetail == null || quantityReceived == null) {
            return null;
        }
        return quantityReceived - purchaseOrderDetail.getQuantityOrdered();
    }
}
