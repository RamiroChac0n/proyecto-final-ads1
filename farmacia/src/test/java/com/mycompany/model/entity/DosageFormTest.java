package com.mycompany.model.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for DosageForm entity
 * @author ramir
 */
public class DosageFormTest {
    
    public DosageFormTest() {
    }
    
    @Test
    @DisplayName("Must be able to create a DosageForm instance")
    public void testCreateEmptyDosageFormInstance() {  
        DosageForm dosageForm = new DosageForm();
        
        assertNotNull(dosageForm);
    }
    
    @Test
    @DisplayName("DosageForm class must have all required fields with correct types")
    void testDosageFormClassHasRequiredFields() throws NoSuchFieldException {
        Class<?> dosageFormClass = DosageForm.class;
        
        Field formCodeField = dosageFormClass.getDeclaredField("formCode");
        assertEquals(String.class, formCodeField.getType());
        
        Field formNameField = dosageFormClass.getDeclaredField("formName");
        assertEquals(String.class, formNameField.getType());
        
        Field routeAdministrationField = dosageFormClass.getDeclaredField("routeAdministration");
        assertEquals(String.class, routeAdministrationField.getType());
        
        Field createdAtField = dosageFormClass.getDeclaredField("createdAt");
        assertEquals(LocalDateTime.class, createdAtField.getType());
    }
    
    @Test
    @DisplayName("Parameterized constructor must have all required parameters")
    void testParameterizedConstructorHasAllParameters() throws NoSuchMethodException {
        Class<?> dosageFormClass = DosageForm.class;
        
        Constructor<?> constructor = dosageFormClass.getDeclaredConstructor(
            String.class,       // formCode
            String.class,       // formName
            String.class,       // routeAdministration
            LocalDateTime.class // createdAt
        );
        
        assertNotNull(constructor);
        assertEquals(4, constructor.getParameterCount());
    }

    @Test
    @DisplayName("DosageForm class must have all basic methods (getters, setters, equals, hashCode, toString)")
    void testAllBasicMethodsExist() throws NoSuchMethodException {
        Class<?> dosageFormClass = DosageForm.class;
        
        // Getters
        assertNotNull(dosageFormClass.getMethod("getFormCode"));
        assertNotNull(dosageFormClass.getMethod("getFormName"));
        assertNotNull(dosageFormClass.getMethod("getRouteAdministration"));
        assertNotNull(dosageFormClass.getMethod("getCreatedAt"));
        
        // Setters
        assertNotNull(dosageFormClass.getMethod("setFormCode", String.class));
        assertNotNull(dosageFormClass.getMethod("setFormName", String.class));
        assertNotNull(dosageFormClass.getMethod("setRouteAdministration", String.class));
        assertNotNull(dosageFormClass.getMethod("setCreatedAt", LocalDateTime.class));
        
        // equals, hashCode, toString
        assertNotNull(dosageFormClass.getMethod("equals", Object.class));
        assertNotNull(dosageFormClass.getMethod("hashCode"));
        assertNotNull(dosageFormClass.getMethod("toString"));
    }
    
    @Test
    @DisplayName("Builder pattern should work correctly")
    void testBuilderPattern() {
        DosageForm dosageForm = DosageForm.builder()
                .formCode("TAB")
                .formName("Tablet")
                .routeAdministration("Oral")
                .build();
        
        assertNotNull(dosageForm);
        assertEquals("TAB", dosageForm.getFormCode());
        assertEquals("Tablet", dosageForm.getFormName());
        assertEquals("Oral", dosageForm.getRouteAdministration());
    }
    
    @Test
    @DisplayName("Should be able to create with different routes of administration")
    void testDifferentRoutesOfAdministration() {
        DosageForm oralForm = DosageForm.builder()
                .formCode("CAP")
                .formName("Capsule")
                .routeAdministration("Oral")
                .build();
        
        DosageForm injectableForm = DosageForm.builder()
                .formCode("INJ")
                .formName("Injectable")
                .routeAdministration("Intravenous")
                .build();
        
        DosageForm topicalForm = DosageForm.builder()
                .formCode("CRE")
                .formName("Cream")
                .routeAdministration("Topical")
                .build();
        
        assertEquals("Oral", oralForm.getRouteAdministration());
        assertEquals("Intravenous", injectableForm.getRouteAdministration());
        assertEquals("Topical", topicalForm.getRouteAdministration());
    }
}