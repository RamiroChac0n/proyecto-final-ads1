package com.mycompany.service;

import com.mycompany.model.entity.ProductType;
import jakarta.ejb.Local;
import java.util.List;

/**
 * Service interface for ProductType operations
 * @author ramir
 */
@Local
public interface IProductTypeService {
    List<ProductType> findActiveTypes();
    List<ProductType> findAll();
    ProductType findById(String typeCode);
}