package com.mycompany.service;

import com.mycompany.model.entity.ProductCategory;
import jakarta.ejb.Local;
import java.util.List;

/**
 * Service interface for ProductCategory operations
 * @author ramir
 */
@Local
public interface IProductCategoryService {
    List<ProductCategory> findActiveCategories();
    List<ProductCategory> findAll();
    ProductCategory findById(String categoryCode);
}