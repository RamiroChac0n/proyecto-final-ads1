package com.mycompany.service.impl;

import com.mycompany.model.entity.ProductCategory;
import com.mycompany.repository.ProductCategoryRepository;
import com.mycompany.service.IProductCategoryService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;

/**
 * Service implementation for ProductCategory operations
 * @author ramir
 */
@Stateless
public class ProductCategoryServiceImpl implements IProductCategoryService {

    @EJB
    private ProductCategoryRepository productCategoryRepository;

    @Override
    public List<ProductCategory> findActiveCategories() {
        return productCategoryRepository.findActiveCategories();
    }

    @Override
    public List<ProductCategory> findAll() {
        return productCategoryRepository.findAll();
    }

    @Override
    public ProductCategory findById(String categoryCode) {
        return productCategoryRepository.findById(categoryCode);
    }
}