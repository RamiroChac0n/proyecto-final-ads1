package com.mycompany.controller.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * JSF Converter for LocalTime
 * Converts between LocalTime and String representation
 * @author ramir
 */
@FacesConverter(value = "localTimeConverter", managed = true)
public class LocalTimeConverter implements Converter<LocalTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public LocalTime getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(value, FORMATTER);
        } catch (DateTimeParseException e) {
            // Try ISO format as fallback
            try {
                return LocalTime.parse(value);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, LocalTime value) {
        if (value == null) {
            return "";
        }
        return value.format(FORMATTER);
    }
}
