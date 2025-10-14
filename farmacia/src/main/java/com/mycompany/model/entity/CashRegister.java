package com.mycompany.model.entity;

import com.mycompany.model.entity.enums.CashRegisterStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

/**
 * Entity for cash register (caja registradora)
 * Tracks opening/closing of cash registers by branch
 * @author ramir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cash_registers")
public class CashRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "register_id")
    private Integer registerId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", referencedColumnName = "branch_id")
    private Branch branch;

    @NotNull
    @Column(name = "opening_date", nullable = false)
    private LocalDate openingDate;

    @NotNull
    @Column(name = "opening_time", nullable = false)
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opening_user", referencedColumnName = "id")
    private User openingUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closing_user", referencedColumnName = "id")
    private User closingUser;

    @NotNull
    @PositiveOrZero
    @Column(name = "initial_cash", nullable = false, precision = 10, scale = 2)
    private BigDecimal initialCash;

    @Column(name = "final_cash", precision = 10, scale = 2)
    private BigDecimal finalCash;

    @Builder.Default
    @Column(name = "total_sales", precision = 10, scale = 2)
    private BigDecimal totalSales = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_transactions")
    private Integer totalTransactions = 0;

    @Column(name = "cash_difference", precision = 10, scale = 2)
    private BigDecimal cashDifference;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    private CashRegisterStatus status = CashRegisterStatus.OPEN;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @OneToMany(mappedBy = "cashRegister", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Sale> sales;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        if (openingDate == null) {
            openingDate = LocalDate.now();
        }
        if (openingTime == null) {
            openingTime = LocalTime.now();
        }
    }
}
