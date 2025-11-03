package com.mycompany.controller.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * JSF Converter for {@link LocalDate} used in date input components.
 * <p>
 * This managed converter enables bidirectional conversion between LocalDate objects
 * and their string representations in JSF date input components. It uses the
 * dd/MM/yyyy format pattern for user-friendly date display and provides fallback
 * to ISO format (yyyy-MM-dd) for programmatic inputs.
 * </p>
 *
 * <h3>Date Format:</h3>
 * <ul>
 *   <li><strong>Primary Format:</strong> dd/MM/yyyy (e.g., "25/12/2024")</li>
 *   <li><strong>Fallback Format:</strong> ISO 8601 (e.g., "2024-12-25")</li>
 * </ul>
 *
 * <h3>Conversion Logic:</h3>
 * <ul>
 *   <li><strong>String to Object:</strong> Parses date string to LocalDate, trying primary format first, then ISO format</li>
 *   <li><strong>Object to String:</strong> Formats LocalDate to dd/MM/yyyy string for UI display</li>
 * </ul>
 *
 * <h3>Error Handling:</h3>
 * <ul>
 *   <li>Null or empty string input returns {@code null} object</li>
 *   <li>Invalid date formats return {@code null} instead of throwing exceptions</li>
 *   <li>Null LocalDate object returns empty string</li>
 * </ul>
 *
 * <h3>Usage in XHTML:</h3>
 * <pre>
 * &lt;p:calendar value="#{cashRegisterController.selectedDate}"
 *             converter="localDateConverter"
 *             pattern="dd/MM/yyyy"
 *             showOn="button" /&gt;
 * </pre>
 *
 * @author ramir
 * @version 1.0
 * @see LocalDate
 * @see DateTimeFormatter
 */
@FacesConverter(value = "localDateConverter", managed = true)
public class LocalDateConverter implements Converter<LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Converts a string representation to a LocalDate object.
     * <p>
     * This method attempts to parse the input string using the primary format (dd/MM/yyyy).
     * If that fails, it falls back to ISO 8601 format (yyyy-MM-dd). If both formats fail,
     * it returns {@code null} instead of throwing an exception, allowing graceful degradation.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being processed
     * @param value     The string value (e.g., "25/12/2024" or "2024-12-25") submitted from the UI
     * @return The corresponding {@link LocalDate} object, or {@code null} if value is null/empty
     *         or cannot be parsed in either format
     * @see LocalDate#parse(CharSequence, DateTimeFormatter)
     */
    @Override
    public LocalDate getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value, FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    /**
     * Converts a LocalDate object to its string representation.
     * <p>
     * This method formats the LocalDate using the dd/MM/yyyy pattern for
     * user-friendly display in the UI. This format is commonly used in
     * Latin American regions.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being rendered
     * @param value     The {@link LocalDate} object to convert
     * @return The formatted date string (e.g., "25/12/2024"), or empty string if value is {@code null}
     * @see LocalDate#format(DateTimeFormatter)
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, LocalDate value) {
        if (value == null) {
            return "";
        }
        return value.format(FORMATTER);
    }
}
