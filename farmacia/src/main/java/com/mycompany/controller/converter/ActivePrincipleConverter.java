package com.mycompany.controller.converter;

import com.mycompany.model.entity.ActivePrinciple;
import com.mycompany.service.IActivePrincipleService;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF Converter for ActivePrinciple entity
 * @author ramir
 */
@FacesConverter(value = "activePrincipleConverter", managed = true)
public class ActivePrincipleConverter implements Converter<ActivePrinciple> {

    @EJB
    private IActivePrincipleService activePrincipleService;

    @Override
    public ActivePrinciple getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return activePrincipleService.findById(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ActivePrinciple value) {
        if (value == null) {
            return "";
        }
        return value.getPrincipleCode();
    }
}