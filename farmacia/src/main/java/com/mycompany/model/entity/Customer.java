package com.mycompany.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
 * Entity for customers
 * Stores customer information for sales invoicing
 * @author ramir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;

    @NotBlank(message = "NIT/Tax ID es requerido")
    @Size(max = 20, message = "NIT/Tax ID debe tener máximo 20 caracteres")
    @Column(name = "tax_id", nullable = false, unique = true, length = 20)
    private String taxId;

    @NotBlank(message = "Nombre del cliente es requerido")
    @Size(max = 200, message = "Nombre debe tener máximo 200 caracteres")
    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Size(max = 255, message = "Dirección debe tener máximo 255 caracteres")
    @Column(name = "address", length = 255)
    private String address;

    @Size(max = 20, message = "Teléfono debe tener máximo 20 caracteres")
    @Column(name = "phone", length = 20)
    private String phone;

    @Email(message = "Formato de email inválido")
    @Size(max = 100, message = "Email debe tener máximo 100 caracteres")
    @Column(name = "email", length = 100)
    private String email;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    // Relationship with sales
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Sale> sales;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
