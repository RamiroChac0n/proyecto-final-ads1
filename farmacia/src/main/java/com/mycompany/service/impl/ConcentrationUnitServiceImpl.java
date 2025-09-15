package com.mycompany.service.impl;

import com.mycompany.model.entity.ConcentrationUnit;
import com.mycompany.repository.ConcentrationUnitRepository;
import com.mycompany.service.IConcentrationUnitService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;

/**
 * Service implementation for ConcentrationUnit operations
 * @author ramir
 */
@Stateless
public class ConcentrationUnitServiceImpl implements IConcentrationUnitService {

    @EJB
    private ConcentrationUnitRepository concentrationUnitRepository;

    @Override
    public List<ConcentrationUnit> findAllOrderedByName() {
        return concentrationUnitRepository.findAllOrderedByName();
    }

    @Override
    public List<ConcentrationUnit> findAll() {
        return concentrationUnitRepository.findAll();
    }

    @Override
    public ConcentrationUnit findById(String unitCode) {
        return concentrationUnitRepository.findById(unitCode);
    }
}