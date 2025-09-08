package com.mycompany.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

/**
 * Entity for pharmacy branches
 * @author ramir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "branches")
public class Branch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private Integer branchId;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "branch_name", nullable = false, length = 100)
    private String branchName;
    
    @Size(max = 200)
    @Column(name = "address", length = 200)
    private String address;
    
    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<InventoryMovement> inventoryMovements;
}