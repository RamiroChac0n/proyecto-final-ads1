package com.mycompany.controller.converter;

import com.mycompany.model.entity.Branch;
import com.mycompany.service.IBranchService;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF Converter for Branch entities.
 * Converts between Branch objects and their string representation (branchId).
 *
 * @author Pharmacy Management System
 */
@FacesConverter(value = "branchConverter", managed = true)
public class BranchConverter implements Converter<Branch> {

    @EJB
    private IBranchService branchService;

    /**
     * Converts a string representation (branchId) to a Branch entity.
     *
     * @param context the FacesContext
     * @param component the UIComponent
     * @param value the string value (branchId)
     * @return the Branch entity, or null if value is null/empty
     */
    @Override
    public Branch getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            Integer branchId = Integer.valueOf(value);
            return branchService.findById(branchId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts a Branch entity to its string representation (branchId).
     *
     * @param context the FacesContext
     * @param component the UIComponent
     * @param value the Branch entity
     * @return the string representation (branchId), or empty string if null
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Branch value) {
        if (value == null || value.getBranchId() == null) {
            return "";
        }
        return value.getBranchId().toString();
    }
}
