package com.mycompany.model.entity.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for MovementType enum
 * @author ramir
 */
public class MovementTypeTest {
    
    public MovementTypeTest() {
    }
    
    @Test
    @DisplayName("MovementType enum should have all expected values")
    public void testMovementTypeEnumValues() {
        MovementType[] values = MovementType.values();
        
        assertEquals(3, values.length);
        
        // Test that all expected values exist
        assertTrue(containsValue(values, MovementType.IN));
        assertTrue(containsValue(values, MovementType.OUT));
        assertTrue(containsValue(values, MovementType.ADJUSTMENT));
    }
    
    @Test
    @DisplayName("MovementType.valueOf should work correctly")
    public void testValueOf() {
        assertEquals(MovementType.IN, MovementType.valueOf("IN"));
        assertEquals(MovementType.OUT, MovementType.valueOf("OUT"));
        assertEquals(MovementType.ADJUSTMENT, MovementType.valueOf("ADJUSTMENT"));
    }
    
    @Test
    @DisplayName("MovementType.valueOf should throw exception for invalid values")
    public void testValueOfWithInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            MovementType.valueOf("INVALID");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            MovementType.valueOf("in"); // Case sensitive
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            MovementType.valueOf("TRANSFER"); // Not a valid movement type
        });
    }
    
    @Test
    @DisplayName("MovementType enum should be case sensitive")
    public void testCaseSensitivity() {
        // These should work (correct case)
        assertDoesNotThrow(() -> MovementType.valueOf("IN"));
        assertDoesNotThrow(() -> MovementType.valueOf("OUT"));
        assertDoesNotThrow(() -> MovementType.valueOf("ADJUSTMENT"));
        
        // These should fail (incorrect case)
        assertThrows(IllegalArgumentException.class, () -> MovementType.valueOf("in"));
        assertThrows(IllegalArgumentException.class, () -> MovementType.valueOf("out"));
        assertThrows(IllegalArgumentException.class, () -> MovementType.valueOf("adjustment"));
        assertThrows(IllegalArgumentException.class, () -> MovementType.valueOf("In"));
        assertThrows(IllegalArgumentException.class, () -> MovementType.valueOf("Out"));
        assertThrows(IllegalArgumentException.class, () -> MovementType.valueOf("Adjustment"));
    }
    
    @Test
    @DisplayName("MovementType enum values should have correct string representation")
    public void testStringRepresentation() {
        assertEquals("IN", MovementType.IN.toString());
        assertEquals("OUT", MovementType.OUT.toString());
        assertEquals("ADJUSTMENT", MovementType.ADJUSTMENT.toString());
    }
    
    @Test
    @DisplayName("MovementType enum should work correctly with name() method")
    public void testNameMethod() {
        assertEquals("IN", MovementType.IN.name());
        assertEquals("OUT", MovementType.OUT.name());
        assertEquals("ADJUSTMENT", MovementType.ADJUSTMENT.name());
    }
    
    @Test
    @DisplayName("MovementType enum should work correctly with ordinal() method")
    public void testOrdinalMethod() {
        assertEquals(0, MovementType.IN.ordinal());
        assertEquals(1, MovementType.OUT.ordinal());
        assertEquals(2, MovementType.ADJUSTMENT.ordinal());
    }
    
    @Test
    @DisplayName("MovementType enum should work correctly with equals and hashCode")
    public void testEqualsAndHashCode() {
        MovementType in1 = MovementType.IN;
        MovementType in2 = MovementType.IN;
        MovementType out = MovementType.OUT;
        
        // Test equals
        assertEquals(in1, in2);
        assertNotEquals(in1, out);
        assertNotEquals(in2, out);
        
        // Test hashCode consistency
        assertEquals(in1.hashCode(), in2.hashCode());
        
        // Different enums should (typically) have different hash codes
        // Note: This is not guaranteed by the contract, but is typical
        assertNotEquals(in1.hashCode(), out.hashCode());
    }
    
    @Test
    @DisplayName("MovementType enum should work correctly in switch statements")
    public void testSwitchStatement() {
        String result;
        
        // Test IN
        result = getMovementDescription(MovementType.IN);
        assertEquals("Entrada de inventario", result);
        
        // Test OUT
        result = getMovementDescription(MovementType.OUT);
        assertEquals("Salida de inventario", result);
        
        // Test ADJUSTMENT
        result = getMovementDescription(MovementType.ADJUSTMENT);
        assertEquals("Ajuste de inventario", result);
    }
    
    @Test
    @DisplayName("MovementType should be compatible with JPA enum storage")
    public void testJPACompatibility() {
        // Test that enum can be converted to string (for JPA storage)
        assertEquals("IN", MovementType.IN.name());
        assertEquals("OUT", MovementType.OUT.name());
        assertEquals("ADJUSTMENT", MovementType.ADJUSTMENT.name());
        
        // Test that enum can be created from string (for JPA retrieval)
        assertEquals(MovementType.IN, MovementType.valueOf("IN"));
        assertEquals(MovementType.OUT, MovementType.valueOf("OUT"));
        assertEquals(MovementType.ADJUSTMENT, MovementType.valueOf("ADJUSTMENT"));
    }
    
    // Helper method to check if an array contains a specific value
    private boolean containsValue(MovementType[] values, MovementType target) {
        for (MovementType value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
    
    // Helper method for testing switch statements
    private String getMovementDescription(MovementType movementType) {
        switch (movementType) {
            case IN:
                return "Entrada de inventario";
            case OUT:
                return "Salida de inventario";
            case ADJUSTMENT:
                return "Ajuste de inventario";
            default:
                return "Tipo de movimiento desconocido";
        }
    }
}