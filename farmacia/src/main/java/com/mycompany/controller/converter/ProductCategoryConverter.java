package com.mycompany.controller.converter;

import com.mycompany.model.entity.ProductCategory;
import com.mycompany.service.IProductCategoryService;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF Converter for {@link ProductCategory} entity used in dropdown components.
 * <p>
 * This managed converter enables bidirectional conversion between ProductCategory entities
 * and their string representations (category codes) in JSF select components. ProductCategory
 * represents therapeutic categories like Analgesic (ANA), Antibiotic (ANT), Cardiovascular (CAR).
 * </p>
 *
 * <h3>Conversion Logic:</h3>
 * <ul>
 *   <li><strong>String to Object:</strong> Converts category code (e.g., "ANA") to ProductCategory entity via service lookup</li>
 *   <li><strong>Object to String:</strong> Converts ProductCategory entity to category code for UI rendering</li>
 * </ul>
 *
 * <h3>Null Handling:</h3>
 * <ul>
 *   <li>Null or empty string input returns {@code null} object</li>
 *   <li>Null ProductCategory object returns empty string</li>
 * </ul>
 *
 * <h3>Usage in XHTML:</h3>
 * <pre>
 * &lt;p:selectOneMenu value="#{productController.product.category}"
 *                  converter="productCategoryConverter"&gt;
 *   &lt;f:selectItems value="#{productController.productCategories}"
 *                  var="cat"
 *                  itemLabel="#{cat.categoryName}"
 *                  itemValue="#{cat}" /&gt;
 * &lt;/p:selectOneMenu&gt;
 * </pre>
 *
 * @author ramir
 * @version 1.0
 * @see ProductCategory
 * @see IProductCategoryService
 */
@FacesConverter(value = "productCategoryConverter", managed = true)
public class ProductCategoryConverter implements Converter<ProductCategory> {

    @EJB
    private IProductCategoryService productCategoryService;

    /**
     * Converts a string representation (category code) to a ProductCategory entity.
     * <p>
     * This method is invoked by JSF during form submission when processing select
     * component values. It queries the database to retrieve the ProductCategory entity
     * corresponding to the submitted category code.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being processed
     * @param value     The string value (category code, e.g., "ANA", "ANT") submitted from the UI
     * @return The corresponding {@link ProductCategory} entity, or {@code null} if value is null/empty
     *         or entity not found
     * @see IProductCategoryService#findById(Object)
     */
    @Override
    public ProductCategory getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return productCategoryService.findById(value);
    }

    /**
     * Converts a ProductCategory entity to its string representation (category code).
     * <p>
     * This method is invoked by JSF during page rendering to convert the ProductCategory
     * object bound to the component into a string value that can be rendered in HTML.
     * The category code serves as the unique identifier for the select option.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being rendered
     * @param value     The {@link ProductCategory} entity to convert
     * @return The category code string (e.g., "ANA"), or empty string if value is {@code null}
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, ProductCategory value) {
        if (value == null) {
            return "";
        }
        return value.getCategoryCode();
    }
}