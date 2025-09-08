package com.mycompany.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity for products with relationships to all lookup tables
 * @author ramir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @NotBlank
    @Size(max = 30)
    @Pattern(regexp = "^[A-Z]{3}-[A-Z]{3}-[A-Z0-9]+-[0-9A-Z]+-[A-Z]{3}-[0-9]{3}$")
    @Column(name = "product_id", length = 30)
    private String productId;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type", referencedColumnName = "type_code")
    private ProductType productType;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code", referencedColumnName = "category_code")
    private ProductCategory category;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "principle_code", referencedColumnName = "principle_code")
    private ActivePrinciple activePrinciple;
    
    @NotBlank
    @Size(max = 10)
    @Column(name = "concentration", nullable = false, length = 10)
    private String concentration;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concentration_unit", referencedColumnName = "unit_code")
    private ConcentrationUnit concentrationUnit;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dosage_form", referencedColumnName = "form_code")
    private DosageForm dosageForm;
    
    @NotBlank
    @Size(max = 200)
    @Column(name = "commercial_name", nullable = false, length = 200)
    private String commercialName;
    
    @Size(max = 100)
    @Column(name = "brand", length = 100)
    private String brand;
    
    @NotBlank
    @Size(max = 200)
    @Column(name = "manufacturer", nullable = false, length = 200)
    private String manufacturer;
    
    @Builder.Default
    @Column(name = "requires_prescription")
    private Boolean requiresPrescription = false;
    
    @Builder.Default
    @Column(name = "min_stock")
    private Integer minStock = 0;
    
    @Builder.Default
    @Column(name = "max_stock")
    private Integer maxStock = 1000;
    
    @Builder.Default
    @Column(name = "current_stock")
    private Integer currentStock = 0;
    
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProductBatch> batches;
    
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<InventoryMovement> inventoryMovements;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}