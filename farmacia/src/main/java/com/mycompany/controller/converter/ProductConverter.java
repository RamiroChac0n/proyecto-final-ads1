package com.mycompany.controller.converter;

import com.mycompany.model.entity.Product;
import com.mycompany.service.IProductService;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF Converter for Product entity
 * Converts between Product objects and their Long IDs
 * @author ramir
 */
@FacesConverter(value = "productConverter", managed = true)
public class ProductConverter implements Converter<Product> {

    @EJB
    private IProductService productService;

    @Override
    public Product getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            Long productId = Long.valueOf(value);
            return productService.findById(productId);
        } catch (NumberFormatException e) {
            // Invalid ID format, return null
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Product value) {
        if (value == null || value.getProductId() == null) {
            return "";
        }
        return value.getProductId().toString();
    }
}
