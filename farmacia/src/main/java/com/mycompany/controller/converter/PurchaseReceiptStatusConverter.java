package com.mycompany.controller.converter;

import com.mycompany.model.entity.enums.PurchaseReceiptStatus;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF Converter for {@link PurchaseReceiptStatus} enum used in dropdown and display components.
 * <p>
 * This managed converter enables bidirectional conversion between PurchaseReceiptStatus enum values
 * and their string representations (enum names) in JSF components. The PurchaseReceiptStatus enum
 * represents the workflow states of purchase receipts: DRAFT (being created) and COMPLETE (finalized).
 * </p>
 *
 * <h3>Conversion Logic:</h3>
 * <ul>
 *   <li><strong>String to Object:</strong> Converts enum name string (e.g., "DRAFT", "COMPLETE") to PurchaseReceiptStatus enum via valueOf()</li>
 *   <li><strong>Object to String:</strong> Converts PurchaseReceiptStatus enum to its name string for UI rendering</li>
 * </ul>
 *
 * <h3>Status Values:</h3>
 * <ul>
 *   <li><strong>DRAFT:</strong> Receipt is being created, products are being registered (editable)</li>
 *   <li><strong>COMPLETE:</strong> Receipt finalized, batches created, inventory updated (read-only)</li>
 * </ul>
 *
 * <h3>Null Handling & Error Recovery:</h3>
 * <ul>
 *   <li>Null or empty string input returns {@code null} object</li>
 *   <li>Invalid enum name (IllegalArgumentException) returns {@code null} instead of throwing</li>
 *   <li>Null PurchaseReceiptStatus object returns empty string</li>
 * </ul>
 *
 * <h3>Usage in XHTML:</h3>
 * <pre>
 * &lt;p:selectOneMenu value="#{purchaseReceiptController.receipt.status}"
 *                  converter="purchaseReceiptStatusConverter"&gt;
 *   &lt;f:selectItems value="#{purchaseReceiptController.statusList}"
 *                  var="status"
 *                  itemLabel="#{status.displayName}"
 *                  itemValue="#{status}" /&gt;
 * &lt;/p:selectOneMenu&gt;
 *
 * &lt;!-- Display status badge --&gt;
 * &lt;p:badge value="#{receipt.status.displayName}"
 *          severity="#{receipt.status.severity}" /&gt;
 * </pre>
 *
 * @author ramir
 * @version 1.0
 * @see PurchaseReceiptStatus
 */
@FacesConverter(value = "purchaseReceiptStatusConverter", managed = true)
public class PurchaseReceiptStatusConverter implements Converter<PurchaseReceiptStatus> {

    /**
     * Converts a string representation (enum name) to a PurchaseReceiptStatus enum value.
     * <p>
     * This method is invoked by JSF during form submission when processing select
     * component values. It uses {@code Enum.valueOf()} to convert the string to the
     * corresponding enum constant and handles invalid enum names gracefully by returning
     * {@code null} instead of throwing an exception.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being processed
     * @param value     The string value (enum name, e.g., "DRAFT", "COMPLETE") submitted from the UI
     * @return The corresponding {@link PurchaseReceiptStatus} enum value, or {@code null} if value
     *         is null/empty or not a valid enum name
     * @see PurchaseReceiptStatus#valueOf(String)
     */
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

    /**
     * Converts a PurchaseReceiptStatus enum value to its string representation (enum name).
     * <p>
     * This method is invoked by JSF during page rendering to convert the PurchaseReceiptStatus
     * enum bound to the component into a string value that can be rendered in HTML. The enum
     * name serves as the unique identifier for the select option.
     * </p>
     * <p>
     * <strong>Note:</strong> This returns the enum name (e.g., "DRAFT", "COMPLETE"), not the
     * display name. To show user-friendly text in the UI, use {@code status.displayName} in
     * the XHTML view.
     * </p>
     *
     * @param context   The {@link FacesContext} for the current request
     * @param component The {@link UIComponent} being rendered
     * @param value     The {@link PurchaseReceiptStatus} enum value to convert
     * @return The enum name string (e.g., "DRAFT"), or empty string if value is {@code null}
     * @see PurchaseReceiptStatus#name()
     * @see PurchaseReceiptStatus#getDisplayName()
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, PurchaseReceiptStatus value) {
        if (value == null) {
            return "";
        }

        return value.name();
    }
}
