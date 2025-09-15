package com.mycompany.service.impl;

import com.mycompany.model.entity.DosageForm;
import com.mycompany.repository.DosageFormRepository;
import com.mycompany.service.IDosageFormService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;

/**
 * Service implementation for DosageForm operations
 * @author ramir
 */
@Stateless
public class DosageFormServiceImpl implements IDosageFormService {

    @EJB
    private DosageFormRepository dosageFormRepository;

    @Override
    public List<DosageForm> findAllOrderedByName() {
        return dosageFormRepository.findAllOrderedByName();
    }

    @Override
    public List<DosageForm> findAll() {
        return dosageFormRepository.findAll();
    }

    @Override
    public DosageForm findById(String formCode) {
        return dosageFormRepository.findById(formCode);
    }
}