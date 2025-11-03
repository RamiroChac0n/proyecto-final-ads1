package com.mycompany.controller.converter;

import com.mycompany.model.entity.Product;
import com.mycompany.service.IProductService;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF Converter for {@link Product} entity used in dropdown and autocomplete components.
 * <p>
 * This managed converter enables bidirectional conversion between Product entities
 * and their string representations (numeric product IDs) in JSF select components.
 * Unlike other entity converters that use string codes, this converter uses Long
 * numeric IDs as the unique identifier.
 * </p>
 *
 * <h3>Conversion Logic:</h3>
 * <ul>
 *   <li><strong>String to Object:</strong> Converts numeric product ID (e.g., "123") to Product entity via service lookup</li>
 *   <li><strong>Object to String:</strong> Converts Product entity to its numeric ID string for UI rendering</li>
 * </ul>
 *
 * <h3>Null Handling & Error Recovery:</h3>
 * <ul>
 *   <li>Null or empty string input returns {@code null} object</li>
 *   <li>Invalid numeric format (NumberFormatException) returns {@code null} instead of throwing</li>
 *   <li>Null Product object or null product ID returns empty string</li>
 * </ul>
 *
 * <h3>Usage in XHTML:</h3>
 * <pre>
 * &lt;p:autoComplete value="#{saleController.selectedProduct}"
 *                  completeMethod="#{saleController.completeProduct}"
 *                  converter="productConverter"
 *                  var="p"
 *                  itemLabel="#{p.commercialName}"
 *                  itemValue="#{p}" /&gt;
 * </pre>
 *
 * @author ramir
 * @version 1.0
 * @see Product
 * @see IProductService
 */
@FacesConverter(value = "productConverter", managed = true)
public class ProductConverter implements Converter<Product> {

    @EJB
    private IProductService productService;

    /**
     * Converts a string representation (numeric product ID) to a Product entity.
     * <p>
     * This method is invoked by JSF during form submission when processing select
     * or autocomplete component values. It parses the string as a Long integer,
     * queries the database to retrieve the Product entity, and handles invalid
     * formats gracefully by returning {@code null}.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being processed
     * @param value     The string value (numeric product ID, e.g., "123") submitted from the UI
     * @return The corresponding {@link Product} entity, or {@code null} if value is null/empty,
     *         invalid format, or entity not found
     * @see IProductService#findById(Long)
     */
    @Override
    public Product getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            Long productId = Long.valueOf(value);
            return productService.findById(productId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts a Product entity to its string representation (numeric product ID).
     * <p>
     * This method is invoked by JSF during page rendering to convert the Product
     * object bound to the component into a string value that can be rendered in HTML.
     * The numeric product ID serves as the unique identifier for the select option.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being rendered
     * @param value     The {@link Product} entity to convert
     * @return The product ID as a string (e.g., "123"), or empty string if value or ID is {@code null}
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Product value) {
        if (value == null || value.getProductId() == null) {
            return "";
        }
        return value.getProductId().toString();
    }
}
