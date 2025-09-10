package com.mycompany.controller.converter;

import com.mycompany.model.entity.ProductCategory;
import com.mycompany.service.IProductCategoryService;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF Converter for ProductCategory entity
 * @author ramir
 */
@FacesConverter(value = "productCategoryConverter", managed = true)
public class ProductCategoryConverter implements Converter<ProductCategory> {

    @EJB
    private IProductCategoryService productCategoryService;

    @Override
    public ProductCategory getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return productCategoryService.findById(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ProductCategory value) {
        if (value == null) {
            return "";
        }
        return value.getCategoryCode();
    }
}