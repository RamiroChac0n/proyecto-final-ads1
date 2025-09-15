package com.mycompany.controller.converter;

import com.mycompany.model.entity.ProductType;
import com.mycompany.service.IProductTypeService;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF Converter for ProductType entity
 * @author ramir
 */
@FacesConverter(value = "productTypeConverter", managed = true)
public class ProductTypeConverter implements Converter<ProductType> {

    @EJB
    private IProductTypeService productTypeService;

    @Override
    public ProductType getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return productTypeService.findById(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ProductType value) {
        if (value == null) {
            return "";
        }
        return value.getTypeCode();
    }
}