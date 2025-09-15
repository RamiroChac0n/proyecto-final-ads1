package com.mycompany.service;

import com.mycompany.model.entity.Product;
import jakarta.ejb.Local;
import java.util.List;

/**
 * Service interface for Product operations
 * @author ramir
 */
@Local
public interface IProductService {
    Product save(Product product);
    Product edit(Product product);
    void delete(Product product);
    List<Product> list();
    List<Product> findActiveProducts();
    Product findById(Long productId);
    List<Product> findByCommercialName(String commercialName);
    List<Product> findByManufacturer(String manufacturer);
    boolean productIdExists(Long productId);
}