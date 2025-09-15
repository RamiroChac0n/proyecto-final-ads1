/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.mycompany.service;

import com.mycompany.model.entity.User;
import jakarta.ejb.Local;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 *
 * @author ramir
 */
public class IUserServiceTest {
    
    private final Class<IUserService> serviceInterface = IUserService.class;

    @Test
    @DisplayName("Must be annotated with EJB's @Local")
    void testLocalAnnotation() {
        assertTrue(serviceInterface.isAnnotationPresent(Local.class),
            "The IUserService interface must be annotated with @Local");
    }

    @Test
    @DisplayName("Must be an interface")
    void testIsInterface() {
        assertTrue(serviceInterface.isInterface(),
            "IUserService must be an interface, not a concrete class");
    }   

    @Test
    @DisplayName("Must have save method with correct signature")
    void testSaveMethodSignature() throws NoSuchMethodException {
        Method method = serviceInterface.getMethod("save", User.class);

        assertEquals(User.class, method.getReturnType(),
            "The save method must return User");
        assertEquals(1, method.getParameterCount(),
            "The save method must have 1 parameter");
        assertEquals(User.class, method.getParameterTypes()[0],
            "The save method's parameter must be User");
        assertTrue(Modifier.isPublic(method.getModifiers()),
            "The save method must be public");
        assertTrue(Modifier.isAbstract(method.getModifiers()),
            "The save method must be abstract");
    }   

    @Test
    @DisplayName("Must have edit method with correct signature")
    void testEditMethodSignature() throws NoSuchMethodException {
        Method method = serviceInterface.getMethod("edit", User.class);

        assertEquals(User.class, method.getReturnType(),
            "The edit method must return User");
        assertEquals(1, method.getParameterCount(),
            "The edit method must have 1 parameter");
        assertEquals(User.class, method.getParameterTypes()[0],
            "The edit method's parameter must be User");
        assertTrue(Modifier.isPublic(method.getModifiers()),
            "The edit method must be public");
        assertTrue(Modifier.isAbstract(method.getModifiers()),
            "The edit method must be abstract");
    }    

    @Test
    @DisplayName("Must have delete method with correct signature")
    void testDeleteMethodSignature() throws NoSuchMethodException {
        Method method = serviceInterface.getMethod("delete", User.class);

        assertEquals(void.class, method.getReturnType(),
            "The delete method must be void");
        assertEquals(1, method.getParameterCount(),
            "The delete method must have 1 parameter");
        assertEquals(User.class, method.getParameterTypes()[0],
            "The delete method's parameter must be User");
        assertTrue(Modifier.isPublic(method.getModifiers()),
            "The delete method must be public");
        assertTrue(Modifier.isAbstract(method.getModifiers()),
            "The delete method must be abstract");
    }   

    @Test
    @DisplayName("Must have list method with correct signature")
    void testListMethodSignature() throws NoSuchMethodException {
        Method method = serviceInterface.getMethod("list");

        assertEquals(List.class, method.getReturnType(),
            "The list method must return List");
        assertEquals(0, method.getParameterCount(),
            "The list method must not have parameters");
        assertTrue(Modifier.isPublic(method.getModifiers()),
            "The list method must be public");
        assertTrue(Modifier.isAbstract(method.getModifiers()),
            "The list method must be abstract");
    }    

    @Test
    @DisplayName("All methods must be public and abstract")
    void testAllMethodsPublicAndAbstract() {
        Method[] methods = serviceInterface.getDeclaredMethods();

        for (Method method : methods) {
            assertTrue(Modifier.isPublic(method.getModifiers()),
                "The method " + method.getName() + " must be public");
            assertTrue(Modifier.isAbstract(method.getModifiers()),
                "The method " + method.getName() + " must be abstract");
        }
    }

    @Test
    @DisplayName("Must be in the correct package")
    void testPackageLocation() {
        assertEquals("com.mycompany.service", serviceInterface.getPackageName(),
            "The interface must be in the package com.mycompany.service");
    }

    @Test
    @DisplayName("Must have the correct name")
    void testInterfaceName() {
        assertEquals("IUserService", serviceInterface.getSimpleName(),
            "The interface must be named IUserService");
    }   
    @Test
    @DisplayName("All method names must be unique")
    void testMethodNamesUnique() {
        Method[] methods = serviceInterface.getDeclaredMethods();
        long uniqueCount = Arrays.stream(methods)
            .map(Method::getName)
            .distinct()
            .count();
        assertEquals(methods.length, uniqueCount,
            "All method names in IUserService must be unique");
    }

    @Test
    @DisplayName("Interface must not declare any fields")
    void testNoDeclaredFields() {
        assertEquals(0, serviceInterface.getDeclaredFields().length,
            "IUserService must not declare any fields");
    }

    @Test
    @DisplayName("Interface must not declare default or static methods")
    void testNoDefaultOrStaticMethods() {
        Method[] methods = serviceInterface.getDeclaredMethods();
        for (Method method : methods) {
            assertFalse(method.isDefault(),
                "Method " + method.getName() + " must not be default");
            assertFalse(Modifier.isStatic(method.getModifiers()),
                "Method " + method.getName() + " must not be static");
        }
    }

    @Test
    @DisplayName("Methods must not throw checked exceptions")
    void testNoCheckedExceptions() {
        Method[] methods = serviceInterface.getDeclaredMethods();
        for (Method method : methods) {
            for (Class<?> exType : method.getExceptionTypes()) {
                boolean isChecked = Exception.class.isAssignableFrom(exType)
                    && !RuntimeException.class.isAssignableFrom(exType);
                assertFalse(isChecked,
                    "Method " + method.getName() + " must not throw checked exceptions");
            }
        }
    }
}
