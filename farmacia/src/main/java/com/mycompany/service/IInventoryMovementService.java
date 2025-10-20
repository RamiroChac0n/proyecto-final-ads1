package com.mycompany.service;

import com.mycompany.model.entity.InventoryMovement;
import com.mycompany.model.entity.Product;
import jakarta.ejb.Local;
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
}
