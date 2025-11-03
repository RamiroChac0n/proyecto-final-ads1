package com.mycompany.controller.converter;

import com.mycompany.model.entity.ProductType;
import com.mycompany.service.IProductTypeService;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF Converter for {@link ProductType} entity used in dropdown components.
 * <p>
 * This managed converter enables bidirectional conversion between ProductType entities
 * and their string representations (type codes) in JSF select components. It integrates
 * with the service layer to fetch entity instances from the database during form processing.
 * </p>
 *
 * <h3>Conversion Logic:</h3>
 * <ul>
 *   <li><strong>String to Object:</strong> Converts type code (e.g., "MED") to ProductType entity via service lookup</li>
 *   <li><strong>Object to String:</strong> Converts ProductType entity to type code for UI rendering</li>
 * </ul>
 *
 * <h3>Null Handling:</h3>
 * <ul>
 *   <li>Null or empty string input returns {@code null} object</li>
 *   <li>Null ProductType object returns empty string</li>
 * </ul>
 *
 * <h3>Usage in XHTML:</h3>
 * <pre>
 * &lt;p:selectOneMenu value="#{productController.product.productType}"
 *                  converter="productTypeConverter"&gt;
 *   &lt;f:selectItems value="#{productController.productTypes}"
 *                  var="type"
 *                  itemLabel="#{type.typeName}"
 *                  itemValue="#{type}" /&gt;
 * &lt;/p:selectOneMenu&gt;
 * </pre>
 *
 * @author ramir
 * @version 1.0
 * @see ProductType
 * @see IProductTypeService
 */
@FacesConverter(value = "productTypeConverter", managed = true)
public class ProductTypeConverter implements Converter<ProductType> {

    @EJB
    private IProductTypeService productTypeService;

    /**
     * Converts a string representation (type code) to a ProductType entity.
     * <p>
     * This method is invoked by JSF during form submission when processing select
     * component values. It queries the database to retrieve the ProductType entity
     * corresponding to the submitted type code.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being processed
     * @param value     The string value (type code, e.g., "MED", "SUP") submitted from the UI
     * @return The corresponding {@link ProductType} entity, or {@code null} if value is null/empty
     *         or entity not found
     * @see IProductTypeService#findById(Object)
     */
    @Override
    public ProductType getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return productTypeService.findById(value);
    }

    /**
     * Converts a ProductType entity to its string representation (type code).
     * <p>
     * This method is invoked by JSF during page rendering to convert the ProductType
     * object bound to the component into a string value that can be rendered in HTML.
     * The type code serves as the unique identifier for the select option.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being rendered
     * @param value     The {@link ProductType} entity to convert
     * @return The type code string (e.g., "MED"), or empty string if value is {@code null}
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, ProductType value) {
        if (value == null) {
            return "";
        }
        return value.getTypeCode();
    }
}