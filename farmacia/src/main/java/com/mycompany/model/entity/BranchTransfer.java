package com.mycompany.model.entity;

import com.mycompany.model.entity.enums.TransferStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;

/**
 * Entity for branch transfers
 * Represents product transfers between branches
 *
 * @author ramir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "branch_transfers")
public class BranchTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Integer transferId;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false)
    private Product product;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id", referencedColumnName = "batch_id", nullable = false)
    private ProductBatch batch;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_branch_id", referencedColumnName = "branch_id", nullable = false)
    private Branch fromBranch;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_branch_id", referencedColumnName = "branch_id", nullable = false)
    private Branch toBranch;

    @NotNull
    @Positive(message = "Quantity must be positive")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "transfer_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transferDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TransferStatus status = TransferStatus.PENDING;

    @Size(max = 50)
    @Column(name = "requested_by", length = 50)
    private String requestedBy;

    @Size(max = 50)
    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @Size(max = 50)
    @Column(name = "received_by", length = 50)
    private String receivedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        if (transferDate == null) {
            transferDate = new Date();
        }
    }
}
