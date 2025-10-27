package com.mycompany.controller.converter;

import com.mycompany.model.entity.enums.PurchaseReceiptStatus;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF Converter for PurchaseReceiptStatus enum.
 * Converts between string representation and enum value for use in JSF components.
 *
 * @author ramir
 */
@FacesConverter(value = "purchaseReceiptStatusConverter", managed = true)
public class PurchaseReceiptStatusConverter implements Converter<PurchaseReceiptStatus> {

    @Override
    public PurchaseReceiptStatus getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return PurchaseReceiptStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, PurchaseReceiptStatus value) {
        if (value == null) {
            return "";
        }

        return value.name();
    }
}
