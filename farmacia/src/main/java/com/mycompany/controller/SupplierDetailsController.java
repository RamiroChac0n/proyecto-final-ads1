package com.mycompany.controller;

import com.mycompany.model.entity.Supplier;
import com.mycompany.model.entity.SupplierContact;
import com.mycompany.service.ISupplierService;
import com.mycompany.service.ISupplierContactService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * JSF Managed Bean controller for viewing and managing supplier details and contacts.
 * <p>
 * This view-scoped controller displays comprehensive information about a specific supplier
 * and manages the supplier's contact persons. It receives the supplierId as a URL parameter
 * and provides functionality to add, edit, and delete supplier contacts.
 * </p>
 *
 * <h3>Core Functionality:</h3>
 * <ul>
 *   <li><strong>View Supplier:</strong> Display complete supplier information (company details, address, NIT, etc.)</li>
 *   <li><strong>Manage Contacts:</strong> CRUD operations for supplier contact persons</li>
 *   <li><strong>Primary Contact:</strong> Flag one contact as the primary contact for the supplier</li>
 * </ul>
 *
 * <h3>Supplier Contact Information:</h3>
 * <ul>
 *   <li>Contact name and position/title</li>
 *   <li>Phone number and email</li>
 *   <li>Primary contact flag (one per supplier)</li>
 *   <li>Notes about the contact</li>
 * </ul>
 *
 * <h3>Navigation:</h3>
 * <p>
 * Accessed from supplier list with URL pattern: {@code supplier-details.xhtml?supplierId=123}
 * </p>
 *
 * <h3>Access Control:</h3>
 * <p>
 * Viewing supplier details is available to all authenticated users. However, only ADMIN
 * users can add, edit, or delete supplier contacts (controlled via rendered attribute in XHTML).
 * </p>
 *
 * <h3>UI Features:</h3>
 * <ul>
 *   <li>Supplier information panel showing all supplier fields</li>
 *   <li>Contacts DataTable with add/edit/delete operations</li>
 *   <li>Primary contact indicator badge</li>
 *   <li>Breadcrumb navigation back to suppliers list</li>
 * </ul>
 *
 * @author ramir
 * @version 1.0
 * @see Supplier
 * @see SupplierContact
 * @see ISupplierService
 * @see ISupplierContactService
 * @see SupplierController
 */
@Data
@Named(value = "supplierDetailsController")
@ViewScoped
public class SupplierDetailsController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ISupplierService supplierService;

    @EJB
    private ISupplierContactService contactService;

    @Inject
    private UserController userController;

    private Supplier supplier;
    private List<SupplierContact> contacts;
    private SupplierContact currentContact;

    /**
     * Initializes the controller after dependency injection is complete.
     * <p>
     * This method extracts the supplierId from the URL query parameter and loads
     * the corresponding supplier details along with their contacts. If the supplierId
     * is invalid or missing, appropriate error messages are displayed.
     * </p>
     *
     * <h4>Initialization Flow:</h4>
     * <ol>
     *   <li>Extracts "supplierId" parameter from URL query string</li>
     *   <li>Validates supplierId is a valid Integer</li>
     *   <li>Loads supplier entity via {@link #loadSupplier(Integer)}</li>
     *   <li>If supplier found, loads associated contacts</li>
     * </ol>
     *
     * <h4>Error Handling:</h4>
     * <ul>
     *   <li><strong>NumberFormatException:</strong> Displays error if supplierId is not a valid integer</li>
     *   <li><strong>Missing parameter:</strong> Page renders without data (graceful degradation)</li>
     * </ul>
     *
     * @see #loadSupplier(Integer)
     * @see #loadContacts()
     */
    @PostConstruct
    public void init() {
        Map<String, String> params = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap();

        String supplierIdParam = params.get("supplierId");

        if (supplierIdParam != null && !supplierIdParam.isEmpty()) {
            try {
                Integer supplierId = Integer.valueOf(supplierIdParam);
                loadSupplier(supplierId);
            } catch (NumberFormatException e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "ID de proveedor inválido"));
            }
        }
    }

    /**
     * Loads supplier details from the database by ID and triggers contact loading.
     * <p>
     * This method retrieves the full supplier entity including all fields (company details,
     * address, NIT, contact information, etc.) and displays them in the view. If the supplier
     * is found, it also loads the associated contacts.
     * </p>
     *
     * <h4>Success Flow:</h4>
     * <ol>
     *   <li>Queries database for supplier with given ID</li>
     *   <li>If found, sets {@code supplier} field and loads contacts</li>
     *   <li>If not found, displays warning message</li>
     * </ol>
     *
     * <h4>Side Effects:</h4>
     * <ul>
     *   <li>Sets {@code supplier} field with loaded entity or null if not found</li>
     *   <li>Triggers {@link #loadContacts()} if supplier found</li>
     *   <li>Displays warning or error message on failure</li>
     * </ul>
     *
     * @param supplierId The unique identifier of the supplier to load
     * @see ISupplierService#findById(Integer)
     * @see #loadContacts()
     */
    public void loadSupplier(Integer supplierId) {
        try {
            supplier = supplierService.findById(supplierId);
            if (supplier == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Advertencia",
                                "Proveedor no encontrado"));
            } else {
                loadContacts();
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "No se pudo cargar el proveedor: " + e.getMessage()));
        }
    }

    /**
     * Loads all contacts associated with the current supplier for display in the DataTable.
     * <p>
     * This method queries the database for all {@link SupplierContact} entities linked to
     * the current supplier. Contacts include information such as contact name, position,
     * phone, email, and whether they are the primary contact.
     * </p>
     *
     * <h4>Side Effects:</h4>
     * <ul>
     *   <li>Sets {@code contacts} field with the retrieved list</li>
     *   <li>Displays error message if loading fails</li>
     * </ul>
     *
     * <h4>Precondition:</h4>
     * <p>
     * {@code supplier} field must not be null (typically set by {@link #loadSupplier(Integer)}).
     * </p>
     *
     * @see ISupplierContactService#findBySupplier(Supplier)
     */
    private void loadContacts() {
        if (supplier != null) {
            try {
                contacts = contactService.findBySupplier(supplier);
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "No se pudieron cargar los contactos: " + e.getMessage()));
            }
        }
    }

    /**
     * Creates a new supplier contact instance with default values for the contact form.
     * <p>
     * This method is called when the user clicks the "New Contact" button to open the
     * contact dialog. It initializes a new {@link SupplierContact} entity associated with
     * the current supplier with default values:
     * </p>
     * <ul>
     *   <li><strong>supplier:</strong> Set to current supplier</li>
     *   <li><strong>isPrimary:</strong> false (user can change to true if desired)</li>
     * </ul>
     *
     * <h4>Side Effects:</h4>
     * <p>
     * Sets the {@code currentContact} field to a new SupplierContact instance ready for user input.
     * </p>
     *
     * @see SupplierContact.SupplierContactBuilder
     */
    public void createNewContact() {
        currentContact = SupplierContact.builder()
                .supplier(supplier)
                .isPrimary(false)
                .build();
    }

    /**
     * Saves the current contact to the database (create or update operation).
     * <p>
     * This method determines whether to create a new contact or update an existing one
     * based on the presence of a contactId. After successful save, it refreshes the
     * contacts list to display the changes.
     * </p>
     *
     * <h4>Operation Logic:</h4>
     * <ul>
     *   <li><strong>contactId = null:</strong> Creates new contact via {@link ISupplierContactService#save(SupplierContact)}</li>
     *   <li><strong>contactId ≠ null:</strong> Updates existing contact via {@link ISupplierContactService#edit(SupplierContact)}</li>
     * </ul>
     *
     * <h4>Success Flow:</h4>
     * <ol>
     *   <li>Persists contact to database</li>
     *   <li>Displays success message (Spanish, operation-specific)</li>
     *   <li>Reloads contacts list to show changes</li>
     * </ol>
     *
     * <h4>Error Handling:</h4>
     * <p>
     * Catches all exceptions and displays user-friendly error message. Common errors
     * include validation failures (missing required fields like contact name).
     * </p>
     *
     * @see ISupplierContactService#save(SupplierContact)
     * @see ISupplierContactService#edit(SupplierContact)
     * @see #loadContacts()
     */
    public void saveContact() {
        try {
            if (currentContact.getContactId() == null) {
                contactService.save(currentContact);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Éxito",
                                "Contacto agregado correctamente"));
            } else {
                contactService.edit(currentContact);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Éxito",
                                "Contacto actualizado correctamente"));
            }
            loadContacts();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "No se pudo guardar el contacto: " + e.getMessage()));
        }
    }

    /**
     * Deletes the current contact from the database.
     * <p>
     * This method removes the selected supplier contact. After successful deletion,
     * it refreshes the contacts list to reflect the removal.
     * </p>
     *
     * <h4>Success Flow:</h4>
     * <ol>
     *   <li>Deletes contact via {@link ISupplierContactService#delete(SupplierContact)}</li>
     *   <li>Displays success message in Spanish</li>
     *   <li>Reloads contacts list to remove deleted entry</li>
     * </ol>
     *
     * <h4>Error Handling:</h4>
     * <p>
     * Catches all exceptions and displays user-friendly error message.
     * </p>
     *
     * <h4>UI Integration:</h4>
     * <p>
     * Typically called from a delete confirmation dialog after user confirms deletion.
     * </p>
     *
     * @see ISupplierContactService#delete(SupplierContact)
     * @see #loadContacts()
     */
    public void deleteContact() {
        try {
            contactService.delete(currentContact);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito",
                            "Contacto eliminado correctamente"));
            loadContacts();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "No se pudo eliminar el contacto: " + e.getMessage()));
        }
    }

    /**
     * Checks if the current user has ADMIN role for conditional UI rendering.
     * <p>
     * This is a convenience method that delegates to {@link UserController#isAdmin()}.
     * It is used in the XHTML view to conditionally render admin-only UI components
     * such as add, edit, and delete buttons for supplier contacts.
     * </p>
     *
     * <h4>Usage in XHTML:</h4>
     * <pre>
     * &lt;p:commandButton value="Add Contact"
     *                  rendered="#{supplierDetailsController.admin}"
     *                  onclick="PF('dlgContact').show()" /&gt;
     * </pre>
     *
     * @return {@code true} if current user is ADMIN, {@code false} otherwise
     * @see UserController#isAdmin()
     */
    public boolean isAdmin() {
        return userController != null && userController.isAdmin();
    }
}
