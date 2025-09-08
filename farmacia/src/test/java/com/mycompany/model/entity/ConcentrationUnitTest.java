package com.mycompany.model.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for ConcentrationUnit entity
 * @author ramir
 */
public class ConcentrationUnitTest {
    
    public ConcentrationUnitTest() {
    }
    
    @Test
    @DisplayName("Must be able to create a ConcentrationUnit instance")
    public void testCreateEmptyConcentrationUnitInstance() {  
        ConcentrationUnit concentrationUnit = new ConcentrationUnit();
        
        assertNotNull(concentrationUnit);
    }
    
    @Test
    @DisplayName("ConcentrationUnit class must have all required fields with correct types")
    void testConcentrationUnitClassHasRequiredFields() throws NoSuchFieldException {
        Class<?> concentrationUnitClass = ConcentrationUnit.class;
        
        Field unitCodeField = concentrationUnitClass.getDeclaredField("unitCode");
        assertEquals(String.class, unitCodeField.getType());
        
        Field unitNameField = concentrationUnitClass.getDeclaredField("unitName");
        assertEquals(String.class, unitNameField.getType());
        
        Field createdAtField = concentrationUnitClass.getDeclaredField("createdAt");
        assertEquals(LocalDateTime.class, createdAtField.getType());
    }
    
    @Test
    @DisplayName("Parameterized constructor must have all required parameters")
    void testParameterizedConstructorHasAllParameters() throws NoSuchMethodException {
        Class<?> concentrationUnitClass = ConcentrationUnit.class;
        
        Constructor<?> constructor = concentrationUnitClass.getDeclaredConstructor(
            String.class,       // unitCode
            String.class,       // unitName
            LocalDateTime.class // createdAt
        );
        
        assertNotNull(constructor);
        assertEquals(3, constructor.getParameterCount());
    }

    @Test
    @DisplayName("ConcentrationUnit class must have all basic methods (getters, setters, equals, hashCode, toString)")
    void testAllBasicMethodsExist() throws NoSuchMethodException {
        Class<?> concentrationUnitClass = ConcentrationUnit.class;
        
        // Getters
        assertNotNull(concentrationUnitClass.getMethod("getUnitCode"));
        assertNotNull(concentrationUnitClass.getMethod("getUnitName"));
        assertNotNull(concentrationUnitClass.getMethod("getCreatedAt"));
        
        // Setters
        assertNotNull(concentrationUnitClass.getMethod("setUnitCode", String.class));
        assertNotNull(concentrationUnitClass.getMethod("setUnitName", String.class));
        assertNotNull(concentrationUnitClass.getMethod("setCreatedAt", LocalDateTime.class));
        
        // equals, hashCode, toString
        assertNotNull(concentrationUnitClass.getMethod("equals", Object.class));
        assertNotNull(concentrationUnitClass.getMethod("hashCode"));
        assertNotNull(concentrationUnitClass.getMethod("toString"));
    }
    
    @Test
    @DisplayName("Builder pattern should work correctly")
    void testBuilderPattern() {
        ConcentrationUnit concentrationUnit = ConcentrationUnit.builder()
                .unitCode("MG")
                .unitName("Miligramos")
                .build();
        
        assertNotNull(concentrationUnit);
        assertEquals("MG", concentrationUnit.getUnitCode());
        assertEquals("Miligramos", concentrationUnit.getUnitName());
    }
    
    @Test
    @DisplayName("Should be able to create different types of concentration units")
    void testDifferentConcentrationUnits() {
        ConcentrationUnit mgUnit = ConcentrationUnit.builder()
                .unitCode("MG")
                .unitName("Miligramos")
                .build();
        
        ConcentrationUnit mlUnit = ConcentrationUnit.builder()
                .unitCode("ML")
                .unitName("Mililitros")
                .build();
        
        ConcentrationUnit uiUnit = ConcentrationUnit.builder()
                .unitCode("UI")
                .unitName("Unidades Internacionales")
                .build();
        
        ConcentrationUnit gUnit = ConcentrationUnit.builder()
                .unitCode("G")
                .unitName("Gramos")
                .build();
        
        assertEquals("MG", mgUnit.getUnitCode());
        assertEquals("Miligramos", mgUnit.getUnitName());
        
        assertEquals("ML", mlUnit.getUnitCode());
        assertEquals("Mililitros", mlUnit.getUnitName());
        
        assertEquals("UI", uiUnit.getUnitCode());
        assertEquals("Unidades Internacionales", uiUnit.getUnitName());
        
        assertEquals("G", gUnit.getUnitCode());
        assertEquals("Gramos", gUnit.getUnitName());
    }
}