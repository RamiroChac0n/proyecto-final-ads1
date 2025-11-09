package com.mycompany.controller.converter;

import com.mycompany.model.entity.ActivePrinciple;
import com.mycompany.service.IActivePrincipleService;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF Converter for {@link ActivePrinciple} entity used in dropdown components.
 * <p>
 * This managed converter enables bidirectional conversion between ActivePrinciple entities
 * and their string representations (principle codes) in JSF select components. ActivePrinciple
 * represents the INN (International Nonproprietary Name) or generic drug name.
 * </p>
 *
 * <h3>Conversion Logic:</h3>
 * <ul>
 *   <li><strong>String to Object:</strong> Converts principle code to ActivePrinciple entity via service lookup</li>
 *   <li><strong>Object to String:</strong> Converts ActivePrinciple entity to principle code for UI rendering</li>
 * </ul>
 *
 * <h3>Null Handling:</h3>
 * <ul>
 *   <li>Null or empty string input returns {@code null} object</li>
 *   <li>Null ActivePrinciple object returns empty string</li>
 * </ul>
 *
 * <h3>Usage in XHTML:</h3>
 * <pre>
 * &lt;p:selectOneMenu value="#{productController.product.activePrinciple}"
 *                  converter="activePrincipleConverter"&gt;
 *   &lt;f:selectItems value="#{productController.activePrinciples}"
 *                  var="principle"
 *                  itemLabel="#{principle.innName}"
 *                  itemValue="#{principle}" /&gt;
 * &lt;/p:selectOneMenu&gt;
 * </pre>
 *
 * @author ramir
 * @version 1.0
 * @see ActivePrinciple
 * @see IActivePrincipleService
 */
@FacesConverter(value = "activePrincipleConverter", managed = true)
public class ActivePrincipleConverter implements Converter<ActivePrinciple> {

    @EJB
    private IActivePrincipleService activePrincipleService;

    /**
     * Converts a string representation (principle code) to an ActivePrinciple entity.
     * <p>
     * This method is invoked by JSF during form submission when processing select
     * component values. It queries the database to retrieve the ActivePrinciple entity
     * corresponding to the submitted principle code.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being processed
     * @param value     The string value (principle code) submitted from the UI
     * @return The corresponding {@link ActivePrinciple} entity, or {@code null} if value is null/empty
     *         or entity not found
     * @see IActivePrincipleService#findById(Object)
     */
    @Override
    public ActivePrinciple getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return activePrincipleService.findById(value);
    }

    /**
     * Converts an ActivePrinciple entity to its string representation (principle code).
     * <p>
     * This method is invoked by JSF during page rendering to convert the ActivePrinciple
     * object bound to the component into a string value that can be rendered in HTML.
     * The principle code serves as the unique identifier for the select option.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being rendered
     * @param value     The {@link ActivePrinciple} entity to convert
     * @return The principle code string, or empty string if value is {@code null}
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, ActivePrinciple value) {
        if (value == null) {
            return "";
        }
        return value.getPrincipleCode();
    }
}