package com.mycompany.service.impl;

import com.mycompany.model.entity.ProductType;
import com.mycompany.repository.ProductTypeRepository;
import com.mycompany.service.IProductTypeService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;

/**
 * Service implementation for ProductType operations
 * @author ramir
 */
@Stateless
public class ProductTypeServiceImpl implements IProductTypeService {

    @EJB
    private ProductTypeRepository productTypeRepository;

    @Override
    public List<ProductType> findActiveTypes() {
        return productTypeRepository.findActiveTypes();
    }

    @Override
    public List<ProductType> findAll() {
        return productTypeRepository.findAll();
    }

    @Override
    public ProductType findById(String typeCode) {
        return productTypeRepository.findById(typeCode);
    }
}