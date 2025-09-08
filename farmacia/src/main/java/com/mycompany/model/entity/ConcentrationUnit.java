package com.mycompany.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for concentration units (mg, ml, UI, etc.)
 * @author ramir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "concentration_units")
public class ConcentrationUnit {
    
    @Id
    @NotBlank
    @Size(max = 4)
    @Column(name = "unit_code", length = 4)
    private String unitCode;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "unit_name", nullable = false, length = 50)
    private String unitName;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}