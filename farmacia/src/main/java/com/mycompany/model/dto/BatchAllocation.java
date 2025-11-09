package com.mycompany.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for batch allocation in sales
 * Represents how much quantity is allocated from a specific batch
 * @author ramir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchAllocation {

    /**
     * The batch ID from which stock is allocated
     */
    private Integer batchId;

    /**
     * Quantity allocated from this batch
     */
    private Integer quantity;

    /**
     * Unit price for sale from this batch
     */
    private BigDecimal unitPrice;

    /**
     * Unit cost from this batch (for profit calculation)
     */
    private BigDecimal unitCost;

    /**
     * Batch number for reference
     */
    private String batchNumber;

    /**
     * Expiration date of the batch (for display/logging)
     */
    private java.util.Date expirationDate;
}
