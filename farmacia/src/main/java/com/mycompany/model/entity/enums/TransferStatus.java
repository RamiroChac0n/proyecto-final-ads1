package com.mycompany.model.entity.enums;

/**
 * Enum for branch transfer status
 * Represents the lifecycle of a product transfer between branches
 *
 * @author ramir
 */
public enum TransferStatus {
    /**
     * Transfer has been requested but not yet approved
     */
    PENDING,

    /**
     * Transfer has been approved and is in transit
     * Stock has been reduced from source branch
     */
    IN_TRANSIT,

    /**
     * Transfer has been received at destination branch
     * Stock has been added to destination branch
     */
    COMPLETED,

    /**
     * Transfer has been cancelled
     * If stock was already reduced, it should be reversed
     */
    CANCELLED
}
