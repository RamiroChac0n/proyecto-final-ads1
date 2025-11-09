package com.mycompany.controller.converter;

import com.mycompany.model.entity.DosageForm;
import com.mycompany.service.IDosageFormService;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF Converter for {@link DosageForm} entity used in dropdown components.
 * <p>
 * This managed converter enables bidirectional conversion between DosageForm entities
 * and their string representations (form codes) in JSF select components. DosageForm
 * represents pharmaceutical presentation forms like Tablet, Capsule, Syrup, Injection, Cream.
 * </p>
 *
 * <h3>Conversion Logic:</h3>
 * <ul>
 *   <li><strong>String to Object:</strong> Converts form code to DosageForm entity via service lookup</li>
 *   <li><strong>Object to String:</strong> Converts DosageForm entity to form code for UI rendering</li>
 * </ul>
 *
 * <h3>Null Handling:</h3>
 * <ul>
 *   <li>Null or empty string input returns {@code null} object</li>
 *   <li>Null DosageForm object returns empty string</li>
 * </ul>
 *
 * <h3>Usage in XHTML:</h3>
 * <pre>
 * &lt;p:selectOneMenu value="#{productController.product.dosageForm}"
 *                  converter="dosageFormConverter"&gt;
 *   &lt;f:selectItems value="#{productController.dosageForms}"
 *                  var="form"
 *                  itemLabel="#{form.formName}"
 *                  itemValue="#{form}" /&gt;
 * &lt;/p:selectOneMenu&gt;
 * </pre>
 *
 * @author ramir
 * @version 1.0
 * @see DosageForm
 * @see IDosageFormService
 */
@FacesConverter(value = "dosageFormConverter", managed = true)
public class DosageFormConverter implements Converter<DosageForm> {

    @EJB
    private IDosageFormService dosageFormService;

    /**
     * Converts a string representation (form code) to a DosageForm entity.
     * <p>
     * This method is invoked by JSF during form submission when processing select
     * component values. It queries the database to retrieve the DosageForm entity
     * corresponding to the submitted form code.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being processed
     * @param value     The string value (form code) submitted from the UI
     * @return The corresponding {@link DosageForm} entity, or {@code null} if value is null/empty
     *         or entity not found
     * @see IDosageFormService#findById(Object)
     */
    @Override
    public DosageForm getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return dosageFormService.findById(value);
    }

    /**
     * Converts a DosageForm entity to its string representation (form code).
     * <p>
     * This method is invoked by JSF during page rendering to convert the DosageForm
     * object bound to the component into a string value that can be rendered in HTML.
     * The form code serves as the unique identifier for the select option.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being rendered
     * @param value     The {@link DosageForm} entity to convert
     * @return The form code string, or empty string if value is {@code null}
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, DosageForm value) {
        if (value == null) {
            return "";
        }
        return value.getFormCode();
    }
}