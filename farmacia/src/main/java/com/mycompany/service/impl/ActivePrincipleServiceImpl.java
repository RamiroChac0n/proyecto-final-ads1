package com.mycompany.service.impl;

import com.mycompany.model.entity.ActivePrinciple;
import com.mycompany.repository.ActivePrincipleRepository;
import com.mycompany.service.IActivePrincipleService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;

/**
 * Service implementation for ActivePrinciple operations
 * @author ramir
 */
@Stateless
public class ActivePrincipleServiceImpl implements IActivePrincipleService {

    @EJB
    private ActivePrincipleRepository activePrincipleRepository;

    @Override
    public List<ActivePrinciple> findAllOrderedByName() {
        return activePrincipleRepository.findAllOrderedByName();
    }

    @Override
    public List<ActivePrinciple> findAll() {
        return activePrincipleRepository.findAll();
    }

    @Override
    public ActivePrinciple findById(String principleCode) {
        return activePrincipleRepository.findById(principleCode);
    }
}