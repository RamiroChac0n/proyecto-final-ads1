package com.mycompany.service;

import com.mycompany.model.entity.Branch;
import jakarta.ejb.Local;
import java.util.List;

/**
 * Service interface for managing Branch entities.
 * Provides business logic operations for branch management.
 *
 * @author Pharmacy Management System
 */
@Local
public interface IBranchService {

    /**
     * Persists a new branch to the database.
     *
     * @param branch the branch entity to save
     * @return the saved branch with generated ID
     */
    Branch save(Branch branch);

    /**
     * Updates an existing branch in the database.
     *
     * @param branch the branch entity to update
     * @return the updated branch
     */
    Branch edit(Branch branch);

    /**
     * Deletes a branch from the database.
     *
     * @param branch the branch entity to delete
     */
    void delete(Branch branch);

    /**
     * Retrieves all branches from the database.
     *
     * @return list of all branches
     */
    List<Branch> list();

    /**
     * Finds a branch by its ID.
     *
     * @param id the branch ID
     * @return the branch entity, or null if not found
     */
    Branch findById(Integer id);

    /**
     * Retrieves all active branches from the database.
     *
     * @return list of active branches (isActive = true)
     */
    List<Branch> findActiveBranches();
}
