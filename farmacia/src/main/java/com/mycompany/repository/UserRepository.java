/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.repository;

import com.mycompany.model.entity.User;
import com.mycompany.repository.persistence.PharmacyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

/**
 *
 * @author ramir
 */
@Stateless
public class UserRepository extends PharmacyRepository<User>{

    public UserRepository() {
        super(User.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    public User findByUserName(String userName) {
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.userName = :userName", User.class);
            query.setParameter("userName", userName);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public long countAdminUsers() {
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.role = :role", Long.class);
            query.setParameter("role", com.mycompany.model.entity.enums.Role.ADMIN);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }
        
}
