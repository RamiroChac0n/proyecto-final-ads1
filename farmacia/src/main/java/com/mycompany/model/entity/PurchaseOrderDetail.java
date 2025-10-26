package com.mycompany.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Entity representing a line item in a Purchase Order.
 * Maps to the 'purchase_order_details' table in the database.
 *
 * Each detail represents one product being ordered with its quantity and pricing.
 *
 * @author ramir
 */
@Entity
@Table(name = "purchase_order_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Integer detailId;

    /**
     * Reference to the parent purchase order.
     * Required - detail cannot exist without an order.
     * When order is deleted, details are also deleted (CASCADE).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "La orden de compra es requerida")
    private PurchaseOrder purchaseOrder;

    /**
     * Product being ordered.
     * EAGER fetch to have product info available for display.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Debe seleccionar un producto")
    private Product product;

    /**
     * Quantity of this product being ordered.
     * Must be positive (> 0).
     */
    @Column(name = "quantity_ordered", nullable = false)
    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer quantityOrdered;

    /**
     * Unit cost of the product (what we pay the supplier per unit).
     * Must be >= 0.00
     */
    @Column(name = "unit_cost", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El costo unitario es requerido")
    @DecimalMin(value = "0.00", message = "El costo unitario debe ser mayor o igual a 0")
    private BigDecimal unitCost;

    /**
     * Subtotal for this line (quantity * unitCost).
     * Calculated automatically.
     */
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * Expected sale price per unit when this product is sold to customers.
     * Optional - helps calculate profit margin.
     * Can be null, will be set during receipt if not specified.
     */
    @Column(name = "expected_sale_price", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "El precio de venta debe ser mayor o igual a 0")
    private BigDecimal expectedSalePrice;

    /**
     * Additional notes for this product line.
     * Can include special instructions, packaging info, etc.
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
     * Sets creation timestamp and calculates subtotal.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        calculateSubtotal();
    }

    /**
     * Lifecycle callback - executed before updating entity.
     * Recalculates subtotal in case quantity or unit cost changed.
     */
    @PreUpdate
    protected void onUpdate() {
        calculateSubtotal();
    }

    /**
     * Calculate the subtotal for this line.
     * subtotal = quantity * unitCost
     */
    public void calculateSubtotal() {
        if (quantityOrdered != null && unitCost != null) {
            subtotal = unitCost.multiply(new BigDecimal(quantityOrdered));
        } else {
            subtotal = BigDecimal.ZERO;
        }
    }

    /**
     * Get the profit margin for this product.
     * Only valid if expectedSalePrice is set.
     *
     * @return profit margin as decimal (e.g., 0.25 for 25%), or null if no sale price
     */
    public BigDecimal getProfitMargin() {
        if (expectedSalePrice == null || unitCost == null ||
            unitCost.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal profit = expectedSalePrice.subtract(unitCost);
        return profit.divide(unitCost, 4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Get the profit margin as a percentage string.
     * Useful for display in UI.
     *
     * @return percentage string like "25.00%" or "N/A" if not applicable
     */
    public String getProfitMarginPercent() {
        BigDecimal margin = getProfitMargin();
        if (margin == null) {
            return "N/A";
        }
        return margin.multiply(new BigDecimal("100"))
                    .setScale(2, BigDecimal.ROUND_HALF_UP) + "%";
    }
}
