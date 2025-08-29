/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.mycompany.service.impl;

import com.mycompany.model.entity.User;
import com.mycompany.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

/**
 *
 * @author ramir
 */
public class UserServiceImplTest {
    
    private UserServiceImpl service;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        service = new UserServiceImpl();
        userRepository = Mockito.mock(UserRepository.class);
        // Usa reflexión para inyectar el mock si no tienes setter
        java.lang.reflect.Field field = service.getClass().getDeclaredField("userRepository");
        field.setAccessible(true);
        field.set(service, userRepository);
    }

    @Test
    void testSave() {
        User user = new User();
        Mockito.when(userRepository.save(user)).thenReturn(user);
        assertEquals(user, service.save(user));
    }

    @Test
    void testEdit() {
        User user = new User();
        Mockito.when(userRepository.update(user)).thenReturn(user);
        assertEquals(user, service.edit(user));
    }

    @Test
    void testDelete() {
        User user = new User();
        service.delete(user);
        Mockito.verify(userRepository).delete(user);
    }

    @Test
    void testList() {
        Mockito.when(userRepository.findAll()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), service.list());
    }
    
}
