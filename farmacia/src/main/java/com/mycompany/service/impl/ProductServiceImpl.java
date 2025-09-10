package com.mycompany.service.impl;

import com.mycompany.model.entity.Product;
import com.mycompany.repository.ProductRepository;
import com.mycompany.service.IProductService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;

/**
 * Service implementation for Product operations
 * @author ramir
 */
@Stateless
public class ProductServiceImpl implements IProductService {

    @EJB
    private ProductRepository productRepository;

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product edit(Product product) {
        return productRepository.update(product);
    }

    @Override
    public void delete(Product product) {
        productRepository.delete(product);
    }

    @Override
    public List<Product> list() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> findActiveProducts() {
        return productRepository.findActiveProducts();
    }

    @Override
    public Product findById(String productId) {
        return productRepository.findById(productId);
    }

    @Override
    public List<Product> findByCommercialName(String commercialName) {
        return productRepository.findByCommercialName(commercialName);
    }

    @Override
    public List<Product> findByManufacturer(String manufacturer) {
        return productRepository.findByManufacturer(manufacturer);
    }

    @Override
    public boolean productIdExists(String productId) {
        return productRepository.findById(productId) != null;
    }
}