package com.mycompany.controller.converter;

import com.mycompany.model.entity.Product;
import com.mycompany.service.IProductService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductConverter.
 * Tests JSF converter functionality for Product entity selection.
 */
@ExtendWith(MockitoExtension.class)
class ProductConverterTest {

    @Mock
    private IProductService productService;

    @Mock
    private FacesContext facesContext;

    @Mock
    private UIComponent uiComponent;

    @InjectMocks
    private ProductConverter converter;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .productId(1L)
                .commercialName("Test Product")
                .build();
    }

    // ========== getAsObject Tests ==========

    @Test
    void getAsObject_NullValue_ReturnsNull() {
        // When
        Product result = converter.getAsObject(facesContext, uiComponent, null);

        // Then
        assertNull(result);
        verify(productService, never()).findById(any());
    }

    @Test
    void getAsObject_EmptyString_ReturnsNull() {
        // When
        Product result = converter.getAsObject(facesContext, uiComponent, "");

        // Then
        assertNull(result);
        verify(productService, never()).findById(any());
    }

    @Test
    void getAsObject_WhitespaceString_ReturnsNull() {
        // When
        Product result = converter.getAsObject(facesContext, uiComponent, "   ");

        // Then
        assertNull(result);
        verify(productService, never()).findById(any());
    }

    @Test
    void getAsObject_ValidProductId_ReturnsProduct() {
        // Given
        when(productService.findById(1L)).thenReturn(testProduct);

        // When
        Product result = converter.getAsObject(facesContext, uiComponent, "1");

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals("Test Product", result.getCommercialName());
        verify(productService).findById(1L);
    }

    @Test
    void getAsObject_InvalidNumberFormat_ReturnsNull() {
        // When
        Product result = converter.getAsObject(facesContext, uiComponent, "invalid");

        // Then
        assertNull(result);
        verify(productService, never()).findById(any());
    }

    @Test
    void getAsObject_NonExistentProductId_ReturnsNull() {
        // Given
        when(productService.findById(999L)).thenReturn(null);

        // When
        Product result = converter.getAsObject(facesContext, uiComponent, "999");

        // Then
        assertNull(result);
        verify(productService).findById(999L);
    }

    // ========== getAsString Tests ==========

    @Test
    void getAsString_NullProduct_ReturnsEmptyString() {
        // When
        String result = converter.getAsString(facesContext, uiComponent, null);

        // Then
        assertEquals("", result);
    }

    @Test
    void getAsString_ProductWithNullId_ReturnsEmptyString() {
        // Given
        Product productWithNullId = Product.builder()
                .productId(null)
                .commercialName("Product Without ID")
                .build();

        // When
        String result = converter.getAsString(facesContext, uiComponent, productWithNullId);

        // Then
        assertEquals("", result);
    }

    @Test
    void getAsString_ValidProduct_ReturnsProductIdString() {
        // When
        String result = converter.getAsString(facesContext, uiComponent, testProduct);

        // Then
        assertEquals("1", result);
    }

    @Test
    void getAsString_ProductWithLargeId_ReturnsCorrectString() {
        // Given
        Product productWithLargeId = Product.builder()
                .productId(999999999L)
                .commercialName("Product With Large ID")
                .build();

        // When
        String result = converter.getAsString(facesContext, uiComponent, productWithLargeId);

        // Then
        assertEquals("999999999", result);
    }
}
