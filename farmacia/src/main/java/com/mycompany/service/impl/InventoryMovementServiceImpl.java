package com.mycompany.service.impl;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.InventoryMovement;
import com.mycompany.model.entity.Product;
import com.mycompany.repository.InventoryMovementRepository;
import com.mycompany.service.IInventoryMovementService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementation for InventoryMovement operations
 * @author ramir
 */
@Stateless
public class InventoryMovementServiceImpl implements IInventoryMovementService {

    @EJB
    private InventoryMovementRepository inventoryMovementRepository;

    @Override
    public InventoryMovement save(InventoryMovement inventoryMovement) {
        return inventoryMovementRepository.save(inventoryMovement);
    }

    @Override
    public InventoryMovement edit(InventoryMovement inventoryMovement) {
        return inventoryMovementRepository.update(inventoryMovement);
    }

    @Override
    public void delete(InventoryMovement inventoryMovement) {
        inventoryMovementRepository.delete(inventoryMovement);
    }

    @Override
    public List<InventoryMovement> list() {
        return inventoryMovementRepository.findAll();
    }

    @Override
    public InventoryMovement findById(Integer movementId) {
        return inventoryMovementRepository.findById(movementId);
    }

    @Override
    public List<InventoryMovement> findByProduct(Product product) {
        return inventoryMovementRepository.findByProduct(product);
    }

    @Override
    public List<InventoryMovement> findByProductId(Long productId) {
        return inventoryMovementRepository.findByProductId(productId);
    }

    @Override
    public List<InventoryMovement> findByBranch(Branch branch) {
        return inventoryMovementRepository.findByBranch(branch);
    }

    @Override
    public List<InventoryMovement> findByBranchAndProduct(Branch branch, Product product) {
        return inventoryMovementRepository.findByBranchAndProduct(branch, product);
    }

    @Override
    public List<InventoryMovement> findByBranchAndDateRange(Branch branch, LocalDateTime fromDate, LocalDateTime toDate) {
        return inventoryMovementRepository.findByBranchAndDateRange(branch, fromDate, toDate);
    }
}
