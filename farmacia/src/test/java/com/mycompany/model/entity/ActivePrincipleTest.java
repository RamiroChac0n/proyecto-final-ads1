package com.mycompany.model.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for ActivePrinciple entity
 * @author ramir
 */
public class ActivePrincipleTest {
    
    public ActivePrincipleTest() {
    }
    
    @Test
    @DisplayName("Must be able to create an ActivePrinciple instance")
    public void testCreateEmptyActivePrincipleInstance() {  
        ActivePrinciple activePrinciple = new ActivePrinciple();
        
        assertNotNull(activePrinciple);
    }
    
    @Test
    @DisplayName("ActivePrinciple class must have all required fields with correct types")
    void testActivePrincipleClassHasRequiredFields() throws NoSuchFieldException {
        Class<?> activePrincipleClass = ActivePrinciple.class;
        
        Field principleCodeField = activePrincipleClass.getDeclaredField("principleCode");
        assertEquals(String.class, principleCodeField.getType());
        
        Field innNameField = activePrincipleClass.getDeclaredField("innName");
        assertEquals(String.class, innNameField.getType());
        
        Field therapeuticActionField = activePrincipleClass.getDeclaredField("therapeuticAction");
        assertEquals(String.class, therapeuticActionField.getType());
        
        Field contraindicationsField = activePrincipleClass.getDeclaredField("contraindications");
        assertEquals(String.class, contraindicationsField.getType());
        
        Field requiresPrescriptionField = activePrincipleClass.getDeclaredField("requiresPrescription");
        assertEquals(Boolean.class, requiresPrescriptionField.getType());
        
        Field createdAtField = activePrincipleClass.getDeclaredField("createdAt");
        assertEquals(LocalDateTime.class, createdAtField.getType());
    }
    
    @Test
    @DisplayName("Parameterized constructor must have all required parameters")
    void testParameterizedConstructorHasAllParameters() throws NoSuchMethodException {
        Class<?> activePrincipleClass = ActivePrinciple.class;
        
        Constructor<?> constructor = activePrincipleClass.getDeclaredConstructor(
            String.class,       // principleCode
            String.class,       // innName
            String.class,       // therapeuticAction
            String.class,       // contraindications
            Boolean.class,      // requiresPrescription
            LocalDateTime.class // createdAt
        );
        
        assertNotNull(constructor);
        assertEquals(6, constructor.getParameterCount());
    }

    @Test
    @DisplayName("ActivePrinciple class must have all basic methods (getters, setters, equals, hashCode, toString)")
    void testAllBasicMethodsExist() throws NoSuchMethodException {
        Class<?> activePrincipleClass = ActivePrinciple.class;
        
        // Getters
        assertNotNull(activePrincipleClass.getMethod("getPrincipleCode"));
        assertNotNull(activePrincipleClass.getMethod("getInnName"));
        assertNotNull(activePrincipleClass.getMethod("getTherapeuticAction"));
        assertNotNull(activePrincipleClass.getMethod("getContraindications"));
        assertNotNull(activePrincipleClass.getMethod("getRequiresPrescription"));
        assertNotNull(activePrincipleClass.getMethod("getCreatedAt"));
        
        // Setters
        assertNotNull(activePrincipleClass.getMethod("setPrincipleCode", String.class));
        assertNotNull(activePrincipleClass.getMethod("setInnName", String.class));
        assertNotNull(activePrincipleClass.getMethod("setTherapeuticAction", String.class));
        assertNotNull(activePrincipleClass.getMethod("setContraindications", String.class));
        assertNotNull(activePrincipleClass.getMethod("setRequiresPrescription", Boolean.class));
        assertNotNull(activePrincipleClass.getMethod("setCreatedAt", LocalDateTime.class));
        
        // equals, hashCode, toString
        assertNotNull(activePrincipleClass.getMethod("equals", Object.class));
        assertNotNull(activePrincipleClass.getMethod("hashCode"));
        assertNotNull(activePrincipleClass.getMethod("toString"));
    }
    
    @Test
    @DisplayName("Builder pattern should work correctly")
    void testBuilderPattern() {
        ActivePrinciple activePrinciple = ActivePrinciple.builder()
                .principleCode("PARA500")
                .innName("Paracetamol")
                .therapeuticAction("Analgesic and antipyretic")
                .contraindications("Severe liver disease")
                .requiresPrescription(false)
                .build();
        
        assertNotNull(activePrinciple);
        assertEquals("PARA500", activePrinciple.getPrincipleCode());
        assertEquals("Paracetamol", activePrinciple.getInnName());
        assertEquals("Analgesic and antipyretic", activePrinciple.getTherapeuticAction());
        assertEquals("Severe liver disease", activePrinciple.getContraindications());
        assertFalse(activePrinciple.getRequiresPrescription());
    }
    
    @Test
    @DisplayName("Default values should be set correctly")
    void testDefaultValues() {
        ActivePrinciple activePrinciple = ActivePrinciple.builder()
                .principleCode("ASP500")
                .innName("Acetylsalicylic acid")
                .build();
        
        assertFalse(activePrinciple.getRequiresPrescription()); // Should default to false
    }
}