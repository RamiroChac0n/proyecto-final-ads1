package com.mycompany.service.impl;

import com.mycompany.model.entity.Supplier;
import com.mycompany.model.entity.SupplierContact;
import com.mycompany.repository.SupplierContactRepository;
import com.mycompany.service.ISupplierContactService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;

/**
 * Service implementation for SupplierContact business logic.
 * Provides supplier contact management operations.
 */
@Stateless
public class SupplierContactServiceImpl implements ISupplierContactService {

    @EJB
    private SupplierContactRepository contactRepository;

    @Override
    public SupplierContact save(SupplierContact contact) {
        return contactRepository.save(contact);
    }

    @Override
    public SupplierContact edit(SupplierContact contact) {
        return contactRepository.update(contact);
    }

    @Override
    public void delete(SupplierContact contact) {
        contactRepository.delete(contact);
    }

    @Override
    public List<SupplierContact> list() {
        return contactRepository.findAll();
    }

    @Override
    public SupplierContact findById(Integer contactId) {
        return contactRepository.findById(contactId);
    }

    @Override
    public List<SupplierContact> findBySupplier(Supplier supplier) {
        return contactRepository.findBySupplier(supplier);
    }
}
