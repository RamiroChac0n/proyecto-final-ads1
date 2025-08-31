/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.model.entity;

import com.mycompany.model.entity.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 *
 * @author ramir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="users")
public class User {
    
    @Id
    @Size(min = 13)
    @Column(name = "id", length = 13)
    private String id;
    
    @NotBlank
    @Size(max = 20)
    @Column(name = "first_name", nullable = false, length = 20)
    private String firstName;
    
    @NotBlank
    @Size(max = 20)
    @Column(name = "last_name", nullable = false, length = 20)
    private String lastName;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "user_name", nullable = false, unique = true, length = 50)
    private String userName;
    
    @Size(max = 8)
    @Column(name = "phone_number", length = 8)
    private String phoneNumber;
    
    @Email
    @NotBlank
    @Size(max = 100)
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role")    
    private Role role;
}
