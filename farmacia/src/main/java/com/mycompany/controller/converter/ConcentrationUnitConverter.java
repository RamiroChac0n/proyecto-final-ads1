package com.mycompany.controller.converter;

import com.mycompany.model.entity.ConcentrationUnit;
import com.mycompany.service.IConcentrationUnitService;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF Converter for {@link ConcentrationUnit} entity used in dropdown components.
 * <p>
 * This managed converter enables bidirectional conversion between ConcentrationUnit entities
 * and their string representations (unit codes) in JSF select components. ConcentrationUnit
 * represents pharmaceutical measurement units like mg, mL, %, IU (International Units).
 * </p>
 *
 * <h3>Conversion Logic:</h3>
 * <ul>
 *   <li><strong>String to Object:</strong> Converts unit code (e.g., "mg", "mL") to ConcentrationUnit entity via service lookup</li>
 *   <li><strong>Object to String:</strong> Converts ConcentrationUnit entity to unit code for UI rendering</li>
 * </ul>
 *
 * <h3>Null Handling:</h3>
 * <ul>
 *   <li>Null or empty string input returns {@code null} object</li>
 *   <li>Null ConcentrationUnit object returns empty string</li>
 * </ul>
 *
 * <h3>Usage in XHTML:</h3>
 * <pre>
 * &lt;p:selectOneMenu value="#{productController.product.concentrationUnit}"
 *                  converter="concentrationUnitConverter"&gt;
 *   &lt;f:selectItems value="#{productController.concentrationUnits}"
 *                  var="unit"
 *                  itemLabel="#{unit.unitName}"
 *                  itemValue="#{unit}" /&gt;
 * &lt;/p:selectOneMenu&gt;
 * </pre>
 *
 * @author ramir
 * @version 1.0
 * @see ConcentrationUnit
 * @see IConcentrationUnitService
 */
@FacesConverter(value = "concentrationUnitConverter", managed = true)
public class ConcentrationUnitConverter implements Converter<ConcentrationUnit> {

    @EJB
    private IConcentrationUnitService concentrationUnitService;

    /**
     * Converts a string representation (unit code) to a ConcentrationUnit entity.
     * <p>
     * This method is invoked by JSF during form submission when processing select
     * component values. It queries the database to retrieve the ConcentrationUnit entity
     * corresponding to the submitted unit code.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being processed
     * @param value     The string value (unit code, e.g., "mg", "mL", "%") submitted from the UI
     * @return The corresponding {@link ConcentrationUnit} entity, or {@code null} if value is null/empty
     *         or entity not found
     * @see IConcentrationUnitService#findById(Object)
     */
    @Override
    public ConcentrationUnit getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return concentrationUnitService.findById(value);
    }

    /**
     * Converts a ConcentrationUnit entity to its string representation (unit code).
     * <p>
     * This method is invoked by JSF during page rendering to convert the ConcentrationUnit
     * object bound to the component into a string value that can be rendered in HTML.
     * The unit code serves as the unique identifier for the select option.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being rendered
     * @param value     The {@link ConcentrationUnit} entity to convert
     * @return The unit code string (e.g., "mg"), or empty string if value is {@code null}
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, ConcentrationUnit value) {
        if (value == null) {
            return "";
        }
        return value.getUnitCode();
    }
}