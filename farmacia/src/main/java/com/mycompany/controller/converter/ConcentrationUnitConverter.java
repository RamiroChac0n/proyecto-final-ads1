package com.mycompany.controller.converter;

import com.mycompany.model.entity.ConcentrationUnit;
import com.mycompany.service.IConcentrationUnitService;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF Converter for ConcentrationUnit entity
 * @author ramir
 */
@FacesConverter(value = "concentrationUnitConverter", managed = true)
public class ConcentrationUnitConverter implements Converter<ConcentrationUnit> {

    @EJB
    private IConcentrationUnitService concentrationUnitService;

    @Override
    public ConcentrationUnit getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return concentrationUnitService.findById(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ConcentrationUnit value) {
        if (value == null) {
            return "";
        }
        return value.getUnitCode();
    }
}