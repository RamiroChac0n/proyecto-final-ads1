package com.mycompany.service;

import com.mycompany.model.entity.ConcentrationUnit;
import jakarta.ejb.Local;
import java.util.List;

/**
 * Service interface for ConcentrationUnit operations
 * @author ramir
 */
@Local
public interface IConcentrationUnitService {
    List<ConcentrationUnit> findAllOrderedByName();
    List<ConcentrationUnit> findAll();
    ConcentrationUnit findById(String unitCode);
}