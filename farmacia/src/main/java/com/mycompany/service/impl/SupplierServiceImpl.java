package com.mycompany.service.impl;

import com.mycompany.model.entity.Supplier;
import com.mycompany.repository.SupplierRepository;
import com.mycompany.service.ISupplierService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;

/**
 * Service implementation for Supplier business logic.
 * Provides supplier management operations.
 */
@Stateless
public class SupplierServiceImpl implements ISupplierService {

    @EJB
    private SupplierRepository supplierRepository;

    @Override
    public Supplier save(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    @Override
    public Supplier edit(Supplier supplier) {
        return supplierRepository.update(supplier);
    }

    @Override
    public void delete(Supplier supplier) {
        supplierRepository.delete(supplier);
    }

    @Override
    public List<Supplier> list() {
        return supplierRepository.findAll();
    }

    @Override
    public Supplier findById(Integer supplierId) {
        return supplierRepository.findById(supplierId);
    }

    @Override
    public Supplier findBySupplierCode(String supplierCode) {
        return supplierRepository.findBySupplierCode(supplierCode);
    }

    @Override
    public Supplier findByTaxId(String taxId) {
        return supplierRepository.findByTaxId(taxId);
    }
}
