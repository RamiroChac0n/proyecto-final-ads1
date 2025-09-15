package com.mycompany.service;

import com.mycompany.model.entity.ActivePrinciple;
import jakarta.ejb.Local;
import java.util.List;

/**
 * Service interface for ActivePrinciple operations
 * @author ramir
 */
@Local
public interface IActivePrincipleService {
    List<ActivePrinciple> findAllOrderedByName();
    List<ActivePrinciple> findAll();
    ActivePrinciple findById(String principleCode);
}