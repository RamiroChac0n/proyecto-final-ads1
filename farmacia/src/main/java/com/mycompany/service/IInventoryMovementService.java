package com.mycompany.service;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.InventoryMovement;
import com.mycompany.model.entity.Product;
import jakarta.ejb.Local;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for InventoryMovement operations
 * @author ramir
 */
@Local
public interface IInventoryMovementService {
    InventoryMovement save(InventoryMovement inventoryMovement);
    InventoryMovement edit(InventoryMovement inventoryMovement);
    void delete(InventoryMovement inventoryMovement);
    List<InventoryMovement> list();
    InventoryMovement findById(Integer movementId);
    List<InventoryMovement> findByProduct(Product product);
    List<InventoryMovement> findByProductId(Long productId);

    /**
     * Find all inventory movements for a specific branch
     * @param branch The branch to filter by
     * @return List of inventory movements for the branch
     */
    List<InventoryMovement> findByBranch(Branch branch);

    /**
     * Find inventory movements by branch and product
     * @param branch The branch to filter by
     * @param product The product to find movements for
     * @return List of inventory movements
     */
    List<InventoryMovement> findByBranchAndProduct(Branch branch, Product product);

    /**
     * Find inventory movements by branch and date range
     * @param branch The branch to filter by
     * @param fromDate Start date/time
     * @param toDate End date/time
     * @return List of inventory movements in the date range
     */
    List<InventoryMovement> findByBranchAndDateRange(Branch branch, LocalDateTime fromDate, LocalDateTime toDate);
}
