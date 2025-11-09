package com.mycompany.service.impl;

import com.mycompany.model.entity.Branch;
import com.mycompany.repository.BranchRepository;
import com.mycompany.service.IBranchService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;

/**
 * Service implementation for managing Branch entities.
 * Delegates to BranchRepository for data access operations.
 *
 * @author Pharmacy Management System
 */
@Stateless
public class BranchServiceImpl implements IBranchService {

    @EJB
    private BranchRepository branchRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Branch save(Branch branch) {
        return branchRepository.save(branch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Branch edit(Branch branch) {
        return branchRepository.update(branch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Branch branch) {
        branchRepository.delete(branch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Branch> list() {
        return branchRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Branch findById(Integer id) {
        return branchRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Branch> findActiveBranches() {
        return branchRepository.findActiveBranches();
    }
}
