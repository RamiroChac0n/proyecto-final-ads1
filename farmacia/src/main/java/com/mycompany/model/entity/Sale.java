package com.mycompany.model.entity;

import com.mycompany.model.entity.enums.SaleStatus;
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
 * Entity for sales (cabecera de venta)
 * @author ramir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sale_id")
    private Integer saleId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "sale_number", nullable = false, unique = true, length = 20)
    private String saleNumber;

    @Column(name = "sale_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date saleDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "register_id", referencedColumnName = "register_id")
    private CashRegister cashRegister;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", referencedColumnName = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
    private Customer customer;

    // Customer data (denormalized for backup/reporting)
    @Size(max = 100)
    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Builder.Default
    @Size(max = 20)
    @Column(name = "customer_tax_id", length = 20)
    private String customerNit = "C/F";

    @Size(max = 200)
    @Column(name = "customer_address", length = 200)
    private String customerAddress;

    @Size(max = 20)
    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    // Amounts
    @NotNull
    @PositiveOrZero
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Builder.Default
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Cash payment
    @Column(name = "cash_received", precision = 10, scale = 2)
    private BigDecimal cashReceived;

    @Builder.Default
    @Column(name = "change_given", precision = 10, scale = 2)
    private BigDecimal changeGiven = BigDecimal.ZERO;

    // Status
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "sale_status", length = 20)
    private SaleStatus saleStatus = SaleStatus.COMPLETED;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by", referencedColumnName = "id")
    private User cancelledBy;

    @Column(name = "cancellation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date cancellationDate;

    // Audit
    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "sale", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<SaleDetail> saleDetails;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
        if (saleDate == null) {
            saleDate = new Date();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
