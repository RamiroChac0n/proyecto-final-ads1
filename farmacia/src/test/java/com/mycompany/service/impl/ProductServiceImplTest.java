package com.mycompany.service.impl;

import com.mycompany.model.entity.*;
import com.mycompany.repository.ProductRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductServiceImpl
 * Tests business logic and database operations for Product persistence
 * @author ramir
 */
public class ProductServiceImplTest {
    
    private ProductServiceImpl service;
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        service = new ProductServiceImpl();
        productRepository = Mockito.mock(ProductRepository.class);
        
        // Use reflection to inject the mock repository
        java.lang.reflect.Field field = service.getClass().getDeclaredField("productRepository");
        field.setAccessible(true);
        field.set(service, productRepository);
    }

    @Test
    @DisplayName("Should save product successfully through service layer")
    void testSave() {
        // Given
        Product product = createTestProduct();
        Product savedProduct = createTestProduct();
        
        when(productRepository.save(product)).thenReturn(savedProduct);
        
        // When
        Product result = service.save(product);
        
        // Then
        assertNotNull(result);
        assertEquals(savedProduct, result);
        verify(productRepository).save(product);
    }
    
    @Test
    @DisplayName("Should verify product save operation captures correct data")
    void testSave_VerifyProductData() {
        // Given
        Product product = createTestProduct();
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        
        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        // When
        service.save(product);
        
        // Then
        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();
        
        assertEquals(1L, capturedProduct.getProductId());
        assertEquals("Amoxil", capturedProduct.getCommercialName());
        assertEquals("GlaxoSmithKline", capturedProduct.getManufacturer());
        assertEquals("500", capturedProduct.getConcentration());
        assertTrue(capturedProduct.getRequiresPrescription());
        assertTrue(capturedProduct.getIsActive());
    }

    @Test
    @DisplayName("Should edit product successfully through service layer")
    void testEdit() {
        // Given
        Product product = createTestProduct();
        Product updatedProduct = createTestProduct();
        updatedProduct.setCommercialName("Updated Amoxil");
        
        when(productRepository.update(product)).thenReturn(updatedProduct);
        
        // When
        Product result = service.edit(product);
        
        // Then
        assertNotNull(result);
        assertEquals(updatedProduct, result);
        verify(productRepository).update(product);
    }

    @Test
    @DisplayName("Should delete product successfully through service layer")
    void testDelete() {
        // Given
        Product product = createTestProduct();
        
        // When
        service.delete(product);
        
        // Then
        verify(productRepository).delete(product);
    }

    @Test
    @DisplayName("Should list all products through service layer")
    void testList() {
        // Given
        List<Product> expectedProducts = Arrays.asList(
            createTestProduct(),
            createTestProduct()
        );
        
        when(productRepository.findAll()).thenReturn(expectedProducts);
        
        // When
        List<Product> result = service.list();
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedProducts, result);
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should find active products through service layer")
    void testFindActiveProducts() {
        // Given
        List<Product> expectedProducts = Arrays.asList(createTestProduct());
        
        when(productRepository.findActiveProducts()).thenReturn(expectedProducts);
        
        // When
        List<Product> result = service.findActiveProducts();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedProducts, result);
        verify(productRepository).findActiveProducts();
    }

    @Test
    @DisplayName("Should find product by ID through service layer")
    void testFindById() {
        // Given
        Long productId = 1L;
        Product expectedProduct = createTestProduct();
        
        when(productRepository.findById(productId)).thenReturn(expectedProduct);
        
        // When
        Product result = service.findById(productId);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedProduct, result);
        assertEquals(productId, result.getProductId());
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should return null when product not found by ID")
    void testFindById_NotFound() {
        // Given
        Long productId = 999L;
        
        when(productRepository.findById(productId)).thenReturn(null);
        
        // When
        Product result = service.findById(productId);
        
        // Then
        assertNull(result);
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should find products by commercial name through service layer")
    void testFindByCommercialName() {
        // Given
        String commercialName = "Amoxil";
        List<Product> expectedProducts = Arrays.asList(createTestProduct());
        
        when(productRepository.findByCommercialName(commercialName)).thenReturn(expectedProducts);
        
        // When
        List<Product> result = service.findByCommercialName(commercialName);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedProducts, result);
        verify(productRepository).findByCommercialName(commercialName);
    }

    @Test
    @DisplayName("Should find products by manufacturer through service layer")
    void testFindByManufacturer() {
        // Given
        String manufacturer = "GlaxoSmithKline";
        List<Product> expectedProducts = Arrays.asList(createTestProduct());
        
        when(productRepository.findByManufacturer(manufacturer)).thenReturn(expectedProducts);
        
        // When
        List<Product> result = service.findByManufacturer(manufacturer);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedProducts, result);
        verify(productRepository).findByManufacturer(manufacturer);
    }

    @Test
    @DisplayName("Should verify product exists when productIdExists returns true")
    void testProductIdExists_True() {
        // Given
        Long productId = 1L;
        Product existingProduct = createTestProduct();
        
        when(productRepository.findById(productId)).thenReturn(existingProduct);
        
        // When
        boolean result = service.productIdExists(productId);
        
        // Then
        assertTrue(result);
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should verify product does not exist when productIdExists returns false")
    void testProductIdExists_False() {
        // Given
        Long productId = 999L;
        
        when(productRepository.findById(productId)).thenReturn(null);
        
        // When
        boolean result = service.productIdExists(productId);
        
        // Then
        assertFalse(result);
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should handle empty results gracefully")
    void testFindByCommercialName_EmptyResult() {
        // Given
        String commercialName = "NonExistentProduct";
        
        when(productRepository.findByCommercialName(commercialName)).thenReturn(Collections.emptyList());
        
        // When
        List<Product> result = service.findByCommercialName(commercialName);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findByCommercialName(commercialName);
    }

    @Test
    @DisplayName("Should handle repository exceptions gracefully")
    void testSave_RepositoryException() {
        // Given
        Product product = createTestProduct();
        
        when(productRepository.save(product)).thenThrow(new RuntimeException("Database connection failed"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> service.save(product));
        verify(productRepository).save(product);
    }

    /**
     * Helper method to create a test Product with all required relationships
     */
    private Product createTestProduct() {
        ProductType productType = ProductType.builder()
                .typeCode("MED")
                .typeName("Medicine")
                .isActive(true)
                .build();
        
        ProductCategory category = ProductCategory.builder()
                .categoryCode("ANT")
                .categoryName("Antibiotics")
                .requiresPrescription(true)
                .isActive(true)
                .build();
        
        ActivePrinciple activePrinciple = ActivePrinciple.builder()
                .principleCode("AMOX")
                .innName("Amoxicillin")
                .requiresPrescription(true)
                .build();
        
        ConcentrationUnit unit = ConcentrationUnit.builder()
                .unitCode("MG")
                .unitName("Miligramos")
                .build();
        
        DosageForm form = DosageForm.builder()
                .formCode("TAB")
                .formName("Tablet")
                .routeAdministration("Oral")
                .build();
        
        return Product.builder()
                .productId(1L)
                .productType(productType)
                .category(category)
                .activePrinciple(activePrinciple)
                .concentration("500")
                .concentrationUnit(unit)
                .dosageForm(form)
                .sequenceNumber(1)
                .commercialName("Amoxil")
                .brand("GSK")
                .manufacturer("GlaxoSmithKline")
                .requiresPrescription(true)
                .minStock(10)
                .maxStock(1000)
                .isActive(true)
                .build();
    }
}