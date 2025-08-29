/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.mycompany.repository;

import com.mycompany.model.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
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
public class UserRepositoryTest {
    
    private UserRepository userRepository;
    private EntityManager em;

    @BeforeEach
    void setUp() {
        em = Mockito.mock(EntityManager.class);
        userRepository = Mockito.spy(new UserRepository());
        Mockito.doReturn(em).when(userRepository).getEntityManager();
    }

    @Test
    void testSave() {
        User user = new User();
        userRepository.save(user);
        Mockito.verify(em).persist(user);
    }
    
    @Test
    void testUpdate() {
        User user = new User();
        userRepository.update(user);
        Mockito.verify(em).merge(user);
    }

    @Test
    void testFindById() {
        User user = new User();
        Mockito.when(em.find(User.class, "123")).thenReturn(user);
        User result = userRepository.findById("123");
        assertEquals(user, result);
    }

    @Test
    void testDelete() {
        User user = new User();
        Mockito.when(em.merge(user)).thenReturn(user);
        userRepository.delete(user);
        Mockito.verify(em).remove(user);
    }    
    
    @Test
    void testFindAll() {
        CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
        CriteriaQuery cq = Mockito.mock(CriteriaQuery.class);
        TypedQuery<User> query = Mockito.mock(TypedQuery.class);

        Mockito.when(em.getCriteriaBuilder()).thenReturn(cb);
        Mockito.when(cb.createQuery()).thenReturn(cq);
        Mockito.when(cq.from(User.class)).thenReturn(null);
        Mockito.when(em.createQuery(cq)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(Collections.emptyList());

        List<User> result = userRepository.findAll();
        assertNotNull(result);
    } 
    
}
