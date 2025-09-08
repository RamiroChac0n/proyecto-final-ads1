package com.mycompany.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for pharmaceutical dosage forms
 * @author ramir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "dosage_forms")
public class DosageForm {
    
    @Id
    @NotBlank
    @Size(max = 3)
    @Column(name = "form_code", length = 3)
    private String formCode;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "form_name", nullable = false, length = 50)
    private String formName;
    
    @NotBlank
    @Size(max = 20)
    @Column(name = "route_administration", nullable = false, length = 20)
    private String routeAdministration;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}