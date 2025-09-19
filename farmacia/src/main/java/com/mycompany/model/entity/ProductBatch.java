package com.mycompany.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Entity for product batches with expiration tracking
 * @author ramir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "product_batches",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "batch_number"}))
public class ProductBatch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Integer batchId;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    private Product product;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;
    
    @NotNull
    @PositiveOrZero
    @Column(name = "quantity_received", nullable = false)
    private Integer quantityReceived;
    
    @NotNull
    @PositiveOrZero
    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable;
    
    @NotNull
    @Column(name = "unit_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCost;
    
    @NotNull
    @Column(name = "sale_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal salePrice;
    
    @Column(name = "manufacture_date")
    @Temporal(TemporalType.DATE)
    private Date manufactureDate;

    @NotNull
    @Column(name = "expiration_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date expirationDate;

    @Column(name = "received_date")
    @Temporal(TemporalType.DATE)
    private Date receivedDate;
    
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Builder.Default
    @Column(name = "is_expired")
    private Boolean isExpired = false;
    
    @Builder.Default
    @Column(name = "days_until_expiration")
    private Integer daysUntilExpiration = 0;
    
    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @OneToMany(mappedBy = "batch", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<InventoryMovement> inventoryMovements;
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        if (receivedDate == null) {
            receivedDate = new Date();
        }
    }
}