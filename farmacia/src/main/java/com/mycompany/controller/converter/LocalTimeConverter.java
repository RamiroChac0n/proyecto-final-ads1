package com.mycompany.controller.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * JSF Converter for {@link LocalTime} used in time input components.
 * <p>
 * This managed converter enables bidirectional conversion between LocalTime objects
 * and their string representations in JSF time input components. It uses the
 * HH:mm format pattern (24-hour format) for user-friendly time display and provides
 * fallback to ISO format (HH:mm:ss) for programmatic inputs.
 * </p>
 *
 * <h3>Time Format:</h3>
 * <ul>
 *   <li><strong>Primary Format:</strong> HH:mm (e.g., "14:30", "09:15")</li>
 *   <li><strong>Fallback Format:</strong> ISO 8601 (e.g., "14:30:00")</li>
 * </ul>
 *
 * <h3>Conversion Logic:</h3>
 * <ul>
 *   <li><strong>String to Object:</strong> Parses time string to LocalTime, trying primary format first, then ISO format</li>
 *   <li><strong>Object to String:</strong> Formats LocalTime to HH:mm string for UI display</li>
 * </ul>
 *
 * <h3>Error Handling:</h3>
 * <ul>
 *   <li>Null or empty string input returns {@code null} object</li>
 *   <li>Invalid time formats return {@code null} instead of throwing exceptions</li>
 *   <li>Null LocalTime object returns empty string</li>
 * </ul>
 *
 * <h3>Usage in XHTML:</h3>
 * <pre>
 * &lt;p:calendar value="#{cashRegisterController.openingTime}"
 *             converter="localTimeConverter"
 *             pattern="HH:mm"
 *             timeOnly="true" /&gt;
 * </pre>
 *
 * @author ramir
 * @version 1.0
 * @see LocalTime
 * @see DateTimeFormatter
 */
@FacesConverter(value = "localTimeConverter", managed = true)
public class LocalTimeConverter implements Converter<LocalTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Converts a string representation to a LocalTime object.
     * <p>
     * This method attempts to parse the input string using the primary format (HH:mm).
     * If that fails, it falls back to ISO 8601 format (HH:mm:ss). If both formats fail,
     * it returns {@code null} instead of throwing an exception, allowing graceful degradation.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being processed
     * @param value     The string value (e.g., "14:30" or "14:30:00") submitted from the UI
     * @return The corresponding {@link LocalTime} object, or {@code null} if value is null/empty
     *         or cannot be parsed in either format
     * @see LocalTime#parse(CharSequence, DateTimeFormatter)
     */
    @Override
    public LocalTime getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(value, FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalTime.parse(value);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    /**
     * Converts a LocalTime object to its string representation.
     * <p>
     * This method formats the LocalTime using the HH:mm pattern (24-hour format)
     * for user-friendly display in the UI. This format is widely used in international
     * time representations.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being rendered
     * @param value     The {@link LocalTime} object to convert
     * @return The formatted time string (e.g., "14:30"), or empty string if value is {@code null}
     * @see LocalTime#format(DateTimeFormatter)
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, LocalTime value) {
        if (value == null) {
            return "";
        }
        return value.format(FORMATTER);
    }
}
