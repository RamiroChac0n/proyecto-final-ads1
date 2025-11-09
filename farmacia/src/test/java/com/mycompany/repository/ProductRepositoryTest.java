package com.mycompany.repository;

import com.mycompany.model.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.*;

/**
 * Unit tests for ProductRepository
 * Tests database operations for Product entity persistence
 * @author ramir
 */
public class ProductRepositoryTest {
    
    private ProductRepository productRepository;
    private EntityManager em;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        em = Mockito.mock(EntityManager.class);
        productRepository = new ProductRepository();
        
        // Use reflection to inject the mocked EntityManager into the repository
        java.lang.reflect.Field field = productRepository.getClass().getSuperclass().getDeclaredField("em");
        field.setAccessible(true);
        field.set(productRepository, em);
    }

    @Test
    @DisplayName("Should save product to database")
    void testSave() {
        // Given
        Product product = createTestProduct();
        
        // When
        productRepository.save(product);
        
        // Then
        Mockito.verify(em).persist(product);
    }
    
    @Test
    @DisplayName("Should update existing product in database")
    void testUpdate() {
        // Given
        Product product = createTestProduct();
        
        // When
        productRepository.update(product);
        
        // Then
        Mockito.verify(em).merge(product);
    }

    @Test
    @DisplayName("Should find product by ID from database")
    void testFindById() {
        // Given
        Long productId = 1L;
        Product expectedProduct = createTestProduct();
        expectedProduct.setProductId(productId);
        
        Mockito.when(em.find(Product.class, productId)).thenReturn(expectedProduct);
        
        // When
        Product result = productRepository.findById(productId);
        
        // Then
        assertEquals(expectedProduct, result);
        assertEquals(productId, result.getProductId());
        Mockito.verify(em).find(Product.class, productId);
    }

    @Test
    @DisplayName("Should delete product from database")
    void testDelete() {
        // Given
        Product product = createTestProduct();
        Mockito.when(em.merge(product)).thenReturn(product);
        
        // When
        productRepository.delete(product);
        
        // Then
        Mockito.verify(em).merge(product);
        Mockito.verify(em).remove(product);
    }    
    
    @Test
    @DisplayName("Should find all products from database")
    void testFindAll() {
        // Given
        CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
        CriteriaQuery cq = Mockito.mock(CriteriaQuery.class);
        TypedQuery<Product> query = Mockito.mock(TypedQuery.class);
        List<Product> expectedProducts = Arrays.asList(createTestProduct());

        Mockito.when(em.getCriteriaBuilder()).thenReturn(cb);
        Mockito.when(cb.createQuery()).thenReturn(cq);
        Mockito.when(cq.from(Product.class)).thenReturn(null);
        Mockito.when(em.createQuery(cq)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(expectedProducts);

        // When
        List<Product> result = productRepository.findAll();
        
        // Then
        assertNotNull(result);
        assertEquals(expectedProducts, result);
    }
    
    @Test
    @DisplayName("Should find active products from database")
    void testFindActiveProducts() {
        // Given
        TypedQuery<Product> query = Mockito.mock(TypedQuery.class);
        List<Product> expectedProducts = Arrays.asList(createTestProduct());
        
        Mockito.when(em.createQuery("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.commercialName", Product.class))
               .thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(expectedProducts);
        
        // When
        List<Product> result = productRepository.findActiveProducts();
        
        // Then
        assertNotNull(result);
        assertEquals(expectedProducts, result);
        Mockito.verify(em).createQuery("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.commercialName", Product.class);
        Mockito.verify(query).getResultList();
    }
    
    @Test
    @DisplayName("Should find products by commercial name from database")
    void testFindByCommercialName() {
        // Given
        String commercialName = "Amoxil";
        TypedQuery<Product> query = Mockito.mock(TypedQuery.class);
        List<Product> expectedProducts = Arrays.asList(createTestProduct());
        
        Mockito.when(em.createQuery("SELECT p FROM Product p WHERE LOWER(p.commercialName) LIKE LOWER(:commercialName)", Product.class))
               .thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(expectedProducts);
        
        // When
        List<Product> result = productRepository.findByCommercialName(commercialName);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedProducts, result);
        Mockito.verify(em).createQuery("SELECT p FROM Product p WHERE LOWER(p.commercialName) LIKE LOWER(:commercialName)", Product.class);
        Mockito.verify(query).setParameter("commercialName", "%" + commercialName + "%");
        Mockito.verify(query).getResultList();
    }
    
    @Test
    @DisplayName("Should find products by manufacturer from database")
    void testFindByManufacturer() {
        // Given
        String manufacturer = "GSK";
        TypedQuery<Product> query = Mockito.mock(TypedQuery.class);
        List<Product> expectedProducts = Arrays.asList(createTestProduct());
        
        Mockito.when(em.createQuery("SELECT p FROM Product p WHERE LOWER(p.manufacturer) LIKE LOWER(:manufacturer)", Product.class))
               .thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(expectedProducts);
        
        // When
        List<Product> result = productRepository.findByManufacturer(manufacturer);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedProducts, result);
        Mockito.verify(em).createQuery("SELECT p FROM Product p WHERE LOWER(p.manufacturer) LIKE LOWER(:manufacturer)", Product.class);
        Mockito.verify(query).setParameter("manufacturer", "%" + manufacturer + "%");
        Mockito.verify(query).getResultList();
    }
    
    @Test
    @DisplayName("Should handle empty results when finding products")
    void testFindByCommercialName_EmptyResult() {
        // Given
        String commercialName = "NonExistentProduct";
        TypedQuery<Product> query = Mockito.mock(TypedQuery.class);
        
        Mockito.when(em.createQuery(anyString(), eq(Product.class))).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(Collections.emptyList());
        
        // When
        List<Product> result = productRepository.findByCommercialName(commercialName);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
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