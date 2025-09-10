package com.mycompany.controller.converter;

import com.mycompany.model.entity.DosageForm;
import com.mycompany.service.IDosageFormService;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF Converter for DosageForm entity
 * @author ramir
 */
@FacesConverter(value = "dosageFormConverter", managed = true)
public class DosageFormConverter implements Converter<DosageForm> {

    @EJB
    private IDosageFormService dosageFormService;

    @Override
    public DosageForm getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return dosageFormService.findById(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, DosageForm value) {
        if (value == null) {
            return "";
        }
        return value.getFormCode();
    }
}