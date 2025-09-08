package com.mycompany.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for active principles (pharmaceutical active ingredients)
 * @author ramir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "active_principles")
public class ActivePrinciple {
    
    @Id
    @NotBlank
    @Size(max = 10)
    @Column(name = "principle_code", length = 10)
    private String principleCode;
    
    @NotBlank
    @Size(max = 200)
    @Column(name = "inn_name", nullable = false, length = 200)
    private String innName;
    
    @Column(name = "therapeutic_action", columnDefinition = "TEXT")
    private String therapeuticAction;
    
    @Column(name = "contraindications", columnDefinition = "TEXT")
    private String contraindications;
    
    @Builder.Default
    @Column(name = "requires_prescription")
    private Boolean requiresPrescription = false;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}