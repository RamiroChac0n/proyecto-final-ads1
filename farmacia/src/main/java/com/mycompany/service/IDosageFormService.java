package com.mycompany.service;

import com.mycompany.model.entity.DosageForm;
import jakarta.ejb.Local;
import java.util.List;

/**
 * Service interface for DosageForm operations
 * @author ramir
 */
@Local
public interface IDosageFormService {
    List<DosageForm> findAllOrderedByName();
    List<DosageForm> findAll();
    DosageForm findById(String formCode);
}