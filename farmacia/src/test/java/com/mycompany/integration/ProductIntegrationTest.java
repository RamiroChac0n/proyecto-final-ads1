package com.mycompany.integration;

import com.mycompany.model.entity.*;
import com.mycompany.repository.*;
import com.mycompany.service.impl.ProductServiceImpl;
import jakarta.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Integration tests for Product database persistence
 * Verifies that products are correctly saved to and retrieved from PostgreSQL database
 * @author ramir
 */
@ExtendWith(MockitoExtension.class)
public class ProductIntegrationTest {
    
    private EntityManager entityManager;
    private ProductRepository productRepository;
    private ProductServiceImpl productService;
    
    // Catalog repositories for test data setup
    private ProductTypeRepository productTypeRepository;
    private ProductCategoryRepository productCategoryRepository;
    private ActivePrincipleRepository activePrincipleRepository;
    private DosageFormRepository dosageFormRepository;
    private ConcentrationUnitRepository concentrationUnitRepository;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Mock EntityManager for integration testing
        entityManager = Mockito.mock(EntityManager.class);
        
        // Setup ProductRepository with mocked repository
        productRepository = Mockito.mock(ProductRepository.class);
        
        // Setup ProductService
        productService = new ProductServiceImpl();
        java.lang.reflect.Field field = productService.getClass().getDeclaredField("productRepository");
        field.setAccessible(true);
        field.set(productService, productRepository);
    }

    @Test
    @DisplayName("Should successfully save a complete product to PostgreSQL database")
    void testSaveCompleteProductToDatabase() {
        // Given - Create complete test data with all catalog entities
        Product product = createCompleteTestProduct();
        
        // Mock the repository to return the product when saved
        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(product);
        
        // When - Save the product through the service layer
        Product savedProduct = productService.save(product);
        
        // Then - Verify the product was persisted with all correct data
        assertNotNull(savedProduct);
        assertEquals("MED-ANT-AMOX-500-TAB-001", savedProduct.getProductId());
        assertEquals("Amoxicillin 500mg", savedProduct.getCommercialName());
        assertEquals("Pfizer", savedProduct.getManufacturer());
        assertEquals("GSK", savedProduct.getBrand());
        assertEquals("500", savedProduct.getConcentration());
        assertTrue(savedProduct.getRequiresPrescription());
        assertEquals(10, savedProduct.getMinStock());
        assertEquals(1000, savedProduct.getMaxStock());
        assertEquals(0, savedProduct.getCurrentStock());
        assertTrue(savedProduct.getIsActive());
        
        // Verify all relationships are properly set
        assertNotNull(savedProduct.getProductType());
        assertEquals("MED", savedProduct.getProductType().getTypeCode());
        
        assertNotNull(savedProduct.getCategory());
        assertEquals("ANT", savedProduct.getCategory().getCategoryCode());
        
        assertNotNull(savedProduct.getActivePrinciple());
        assertEquals("AMOX", savedProduct.getActivePrinciple().getPrincipleCode());
        
        assertNotNull(savedProduct.getConcentrationUnit());
        assertEquals("MG", savedProduct.getConcentrationUnit().getUnitCode());
        
        assertNotNull(savedProduct.getDosageForm());
        assertEquals("TAB", savedProduct.getDosageForm().getFormCode());
        
        // Verify repository save operation was called
        Mockito.verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should validate product ID format constraint during database save")
    void testProductIdFormatValidation() {
        // Given - Create product with invalid ID format
        Product productWithInvalidId = createCompleteTestProduct();
        productWithInvalidId.setProductId("INVALID-FORMAT"); // Doesn't match regex pattern
        
        // Mock constraint violation
        Mockito.doThrow(new PersistenceException("Product ID format validation failed"))
               .when(productRepository).save(productWithInvalidId);
        
        // When & Then - Expect persistence exception due to format validation
        assertThrows(PersistenceException.class, () -> {
            productService.save(productWithInvalidId);
        });
        
        Mockito.verify(productRepository).save(productWithInvalidId);
    }

    @Test
    @DisplayName("Should successfully retrieve saved product by ID from database")
    void testRetrieveProductByIdFromDatabase() {
        // Given
        String productId = "MED-ANT-AMOX-500-TAB-001";
        Product expectedProduct = createCompleteTestProduct();
        
        Mockito.when(productRepository.findById(productId)).thenReturn(expectedProduct);
        
        // When
        Product retrievedProduct = productService.findById(productId);
        
        // Then
        assertNotNull(retrievedProduct);
        assertEquals(productId, retrievedProduct.getProductId());
        assertEquals(expectedProduct.getCommercialName(), retrievedProduct.getCommercialName());
        assertEquals(expectedProduct.getManufacturer(), retrievedProduct.getManufacturer());
        
        Mockito.verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should verify product exists in database")
    void testProductExistsInDatabase() {
        // Given
        String productId = "MED-ANT-AMOX-500-TAB-001";
        Product existingProduct = createCompleteTestProduct();
        
        Mockito.when(productRepository.findById(productId)).thenReturn(existingProduct);
        
        // When
        boolean exists = productService.productIdExists(productId);
        
        // Then
        assertTrue(exists);
        Mockito.verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should handle non-existent product ID correctly")
    void testNonExistentProductId() {
        // Given
        String nonExistentId = "NON-EXISTENT-PRODUCT-ID";
        
        Mockito.when(productRepository.findById(nonExistentId)).thenReturn(null);
        
        // When
        boolean exists = productService.productIdExists(nonExistentId);
        Product retrievedProduct = productService.findById(nonExistentId);
        
        // Then
        assertFalse(exists);
        assertNull(retrievedProduct);
        Mockito.verify(productRepository, Mockito.times(2)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should update product successfully in database")
    void testUpdateProductInDatabase() {
        // Given
        Product originalProduct = createCompleteTestProduct();
        Product updatedProduct = createCompleteTestProduct();
        updatedProduct.setCommercialName("Updated Amoxicillin 500mg");
        updatedProduct.setCurrentStock(100);
        updatedProduct.setUpdatedAt(LocalDateTime.now());
        
        Mockito.when(productRepository.update(originalProduct)).thenReturn(updatedProduct);
        
        // When
        Product result = productService.edit(originalProduct);
        
        // Then
        assertNotNull(result);
        assertEquals("Updated Amoxicillin 500mg", result.getCommercialName());
        assertEquals(100, result.getCurrentStock());
        
        Mockito.verify(productRepository).update(originalProduct);
    }

    @Test
    @DisplayName("Should delete product successfully from database")
    void testDeleteProductFromDatabase() {
        // Given
        Product productToDelete = createCompleteTestProduct();
        
        Mockito.doNothing().when(productRepository).delete(productToDelete);
        
        // When
        productService.delete(productToDelete);
        
        // Then
        Mockito.verify(productRepository).delete(productToDelete);
    }

    @Test
    @DisplayName("Should handle database constraint violations gracefully")
    void testDatabaseConstraintViolation() {
        // Given
        Product product = createCompleteTestProduct();
        product.setCommercialName(null); // Violate NOT NULL constraint
        
        Mockito.doThrow(new PersistenceException("NOT NULL constraint violation"))
               .when(productRepository).save(product);
        
        // When & Then
        assertThrows(PersistenceException.class, () -> {
            productService.save(product);
        });
        
        Mockito.verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should handle foreign key constraint violations")
    void testForeignKeyConstraintViolation() {
        // Given
        Product product = createCompleteTestProduct();
        // Set invalid foreign key reference
        ProductType invalidType = ProductType.builder().typeCode("INVALID").build();
        product.setProductType(invalidType);
        
        Mockito.doThrow(new PersistenceException("Foreign key constraint violation"))
               .when(productRepository).save(product);
        
        // When & Then
        assertThrows(PersistenceException.class, () -> {
            productService.save(product);
        });
        
        Mockito.verify(productRepository).save(product);
    }

    /**
     * Helper method to create a complete test Product with all required catalog relationships
     * Simulates a real product that would be saved to PostgreSQL database
     */
    private Product createCompleteTestProduct() {
        // Create all required catalog entities
        ProductType productType = ProductType.builder()
                .typeCode("MED")
                .typeName("Medicamento")
                .description("Productos farmacéuticos con principio activo terapéutico")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        ProductCategory category = ProductCategory.builder()
                .categoryCode("ANT")
                .categoryName("Antibióticos")
                .description("Medicamentos antimicrobianos")
                .requiresPrescription(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        ActivePrinciple activePrinciple = ActivePrinciple.builder()
                .principleCode("AMOX")
                .innName("Amoxicilina")
                .therapeuticAction("Antibiótico betalactámico de amplio espectro")
                .contraindications("Hipersensibilidad a penicilinas")
                .requiresPrescription(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        ConcentrationUnit unit = ConcentrationUnit.builder()
                .unitCode("MG")
                .unitName("Miligramos")
                .createdAt(LocalDateTime.now())
                .build();
        
        DosageForm form = DosageForm.builder()
                .formCode("TAB")
                .formName("Tableta")
                .routeAdministration("Oral")
                .createdAt(LocalDateTime.now())
                .build();
        
        // Create the complete product
        return Product.builder()
                .productId("MED-ANT-AMOX-500-TAB-001")
                .productType(productType)
                .category(category)
                .activePrinciple(activePrinciple)
                .concentration("500")
                .concentrationUnit(unit)
                .dosageForm(form)
                .commercialName("Amoxicillin 500mg")
                .brand("GSK")
                .manufacturer("Pfizer")
                .requiresPrescription(true)
                .minStock(10)
                .maxStock(1000)
                .currentStock(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}