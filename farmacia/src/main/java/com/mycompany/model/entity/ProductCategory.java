package com.mycompany.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for product categories
 * @author ramir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "product_categories")
public class ProductCategory {
    
    @Id
    @NotBlank
    @Size(max = 3)
    @Column(name = "category_code", length = 3)
    private String categoryCode;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Builder.Default
    @Column(name = "requires_prescription")
    private Boolean requiresPrescription = false;
    
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}